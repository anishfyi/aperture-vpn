package io.github.anishfyi.aperture.ui

import android.app.Application
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.anishfyi.aperture.data.ConnectionState
import io.github.anishfyi.aperture.data.RankedServer
import io.github.anishfyi.aperture.data.ServerProfile
import io.github.anishfyi.aperture.data.VpnGateFetcher
import io.github.anishfyi.aperture.probe.Prober
import io.github.anishfyi.aperture.probe.Scorer
import io.github.anishfyi.aperture.vpn.ConnectionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ApertureUiState(
    val rankedServers: List<RankedServer> = emptyList(),
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val activeServer: ServerProfile? = null,
    val statusMessage: String = "",
    val isRefreshing: Boolean = false,
    val profileCount: Int = 0,
    val errorMessage: String? = null,
    val setupComplete: Boolean = false,
    val setupStatus: String = "Starting up",
)

class ApertureViewModel(application: Application) : AndroidViewModel(application) {
    private val fetcher = VpnGateFetcher(application)
    private val prober = Prober()
    private val connectionManager = ConnectionManager(application)

    private val _uiState = MutableStateFlow(ApertureUiState())
    val uiState: StateFlow<ApertureUiState> = _uiState.asStateFlow()

    private var pendingConnect: (() -> Unit)? = null
    private var vpnPermissionLauncher: ((Intent) -> Unit)? = null

    init {
        viewModelScope.launch {
            connectionManager.connectionState.collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }
        viewModelScope.launch {
            connectionManager.activeServer.collect { server ->
                _uiState.update { it.copy(activeServer = server) }
            }
        }
        viewModelScope.launch {
            connectionManager.statusMessage.collect { message ->
                _uiState.update { it.copy(statusMessage = message) }
            }
        }
    }

    fun setVpnPermissionLauncher(launcher: (Intent) -> Unit) {
        vpnPermissionLauncher = launcher
    }

    fun bootstrap() {
        if (_uiState.value.setupComplete) return
        viewModelScope.launch {
            _uiState.update { it.copy(setupStatus = "Loading server list", isRefreshing = true) }
            runCatching {
                val profiles = fetcher.getProfiles(forceRefresh = true)
                _uiState.update {
                    it.copy(setupStatus = "Testing ${profiles.size} servers", profileCount = profiles.size)
                }
                val probes = prober.probeAll(profiles)
                val ranked = Scorer.rank(profiles, probes)
                _uiState.update {
                    it.copy(
                        rankedServers = ranked,
                        profileCount = profiles.size,
                        isRefreshing = false,
                        setupComplete = true,
                        setupStatus = "Ready",
                    )
                }
            }.onFailure { error ->
                // Do not trap the user on the setup screen: enter the app and
                // surface the error there so they can retry from Select location.
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        setupComplete = true,
                        errorMessage = error.message ?: "Could not load servers",
                        statusMessage = "Could not load servers, pull to refresh",
                    )
                }
            }
        }
    }

    fun refresh(force: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            runCatching {
                val profiles = fetcher.getProfiles(forceRefresh = force)
                val probes = prober.probeAll(profiles)
                val ranked = Scorer.rank(profiles, probes)
                _uiState.update {
                    it.copy(
                        rankedServers = ranked,
                        profileCount = profiles.size,
                        isRefreshing = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = error.message ?: "Refresh failed",
                    )
                }
            }
        }
    }

    fun toggleConnection() {
        val state = _uiState.value.connectionState
        if (state == ConnectionState.CONNECTED || state == ConnectionState.CONNECTING) {
            connectionManager.disconnect()
            return
        }
        connectBest()
    }

    fun connectTo(server: ServerProfile) {
        withVpnPermission {
            viewModelScope.launch {
                connectionManager.connect(server)
            }
        }
    }

    fun connectBest() {
        withVpnPermission {
            viewModelScope.launch {
                val ranked = _uiState.value.rankedServers
                connectionManager.connectSmart(ranked)
            }
        }
    }

    fun onVpnPermissionResult(granted: Boolean) {
        if (granted) {
            pendingConnect?.invoke()
        } else {
            _uiState.update {
                it.copy(
                    connectionState = ConnectionState.ERROR,
                    statusMessage = "VPN permission denied",
                )
            }
        }
        pendingConnect = null
    }

    private fun withVpnPermission(action: () -> Unit) {
        val context = getApplication<Application>()
        val intent = VpnService.prepare(context)
        if (intent != null) {
            pendingConnect = action
            vpnPermissionLauncher?.invoke(intent)
        } else {
            action()
        }
    }

    override fun onCleared() {
        connectionManager.dispose()
        super.onCleared()
    }
}
