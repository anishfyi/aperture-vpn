package io.github.anishfyi.aperture.vpn

import android.content.Context
import android.content.Intent
import de.blinkt.openvpn.VpnProfile
import de.blinkt.openvpn.core.ConfigParser
import de.blinkt.openvpn.core.ConnectionStatus
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.ProfileManager
import de.blinkt.openvpn.core.VPNLaunchHelper
import de.blinkt.openvpn.core.VpnStatus
import io.github.anishfyi.aperture.data.ConnectionState
import io.github.anishfyi.aperture.data.OvpnParser
import io.github.anishfyi.aperture.data.RankedServer
import io.github.anishfyi.aperture.data.ServerProfile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.StringReader

class ConnectionManager(private val context: Context) : VpnStatus.StateListener {
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _activeServer = MutableStateFlow<ServerProfile?>(null)
    val activeServer: StateFlow<ServerProfile?> = _activeServer.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private var connectDeferred: CompletableDeferred<Boolean>? = null

    init {
        VpnStatus.addStateListener(this)
    }

    fun dispose() {
        VpnStatus.removeStateListener(this)
    }

    override fun updateState(
        state: String?,
        logmessage: String?,
        localizedResId: Int,
        level: ConnectionStatus?,
        intent: Intent?,
    ) {
        val message = logmessage.orEmpty()
        _statusMessage.value = message
        when (level) {
            ConnectionStatus.LEVEL_CONNECTED -> {
                _connectionState.value = ConnectionState.CONNECTED
                connectDeferred?.complete(true)
            }
            ConnectionStatus.LEVEL_AUTH_FAILED,
            ConnectionStatus.LEVEL_NONETWORK,
            ConnectionStatus.LEVEL_NOTCONNECTED,
            -> {
                if (_connectionState.value == ConnectionState.CONNECTING) {
                    connectDeferred?.complete(false)
                }
                if (level == ConnectionStatus.LEVEL_AUTH_FAILED) {
                    _connectionState.value = ConnectionState.ERROR
                } else if (_connectionState.value != ConnectionState.CONNECTING) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
            }
            ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET,
            ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED,
            ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT,
            -> {
                _connectionState.value = ConnectionState.CONNECTING
            }
            else -> Unit
        }
    }

    override fun setConnectedVPN(uuid: String?) {
        // handled via updateState
    }

    suspend fun connectSmart(ranked: List<RankedServer>): Boolean {
        val candidates = ranked.take(FALLBACK_COUNT)
        if (candidates.isEmpty()) {
            _connectionState.value = ConnectionState.ERROR
            _statusMessage.value = "No reachable servers"
            return false
        }

        for (candidate in candidates) {
            val connected = connect(candidate.profile)
            if (connected) {
                return true
            }
        }
        _connectionState.value = ConnectionState.ERROR
        _statusMessage.value = "All connection attempts failed"
        return false
    }

    suspend fun connect(profile: ServerProfile): Boolean = withContext(Dispatchers.Main) {
        _connectionState.value = ConnectionState.CONNECTING
        _activeServer.value = profile
        _statusMessage.value = "Connecting to ${profile.countryLong}"

        val vpnProfile = buildVpnProfile(profile) ?: run {
            _connectionState.value = ConnectionState.ERROR
            _statusMessage.value = "Invalid OpenVPN profile"
            return@withContext false
        }

        ProfileManager.setTemporaryProfile(context, vpnProfile)
        connectDeferred = CompletableDeferred()
        VPNLaunchHelper.startOpenVpn(vpnProfile, context, "Aperture smart connect", true)

        val success = withTimeoutOrNull(CONNECT_TIMEOUT_MS) {
            connectDeferred?.await() == true
        } ?: false

        if (!success) {
            disconnect()
        }
        success
    }

    fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTING
        val intent = Intent(context, OpenVPNService::class.java)
            .setAction(OpenVPNService.DISCONNECT_VPN)
        context.startService(intent)
        ProfileManager.setConntectedVpnProfileDisconnected(context)
        _connectionState.value = ConnectionState.DISCONNECTED
        _activeServer.value = null
        _statusMessage.value = "Disconnected"
    }

    private fun buildVpnProfile(profile: ServerProfile): VpnProfile? {
        return try {
            val parser = ConfigParser()
            parser.parseConfig(StringReader(profile.ovpnConfig))
            val vpnProfile = parser.convertProfile()
            vpnProfile.mName = "${profile.countryShort}-${profile.ip}"
            vpnProfile.mOverrideDNS = true
            vpnProfile.mDNS1 = OvpnParser.DNS1
            vpnProfile.mDNS2 = OvpnParser.DNS2
            vpnProfile
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val FALLBACK_COUNT = 5
        private const val CONNECT_TIMEOUT_MS = 45_000L
    }
}
