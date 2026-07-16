package io.github.anishfyi.aperture.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.anishfyi.aperture.ui.ApertureViewModel
import io.github.anishfyi.aperture.ui.screens.HomeScreen
import io.github.anishfyi.aperture.ui.screens.ServersScreen
import io.github.anishfyi.aperture.ui.theme.ApertureColors

@Composable
fun ApertureNavHost(viewModel: ApertureViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showLocations by rememberSaveable { mutableStateOf(false) }

    Scaffold(containerColor = ApertureColors.Background) { padding ->
        if (showLocations) {
            BackHandler { showLocations = false }
            ServersScreen(
                uiState = uiState,
                onRefresh = { viewModel.refresh(force = true) },
                onConnect = { server ->
                    showLocations = false
                    viewModel.connectTo(server)
                },
                onBack = { showLocations = false },
                modifier = Modifier.padding(padding),
            )
        } else {
            HomeScreen(
                uiState = uiState,
                onToggleConnection = viewModel::toggleConnection,
                onSelectLocation = { showLocations = true },
                modifier = Modifier.padding(padding),
            )
        }
    }
}
