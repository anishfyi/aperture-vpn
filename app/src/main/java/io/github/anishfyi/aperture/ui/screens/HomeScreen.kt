package io.github.anishfyi.aperture.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.anishfyi.aperture.data.ConnectionState
import io.github.anishfyi.aperture.ui.ApertureUiState
import io.github.anishfyi.aperture.ui.components.GlobeBackground
import io.github.anishfyi.aperture.ui.theme.ApertureColors

@Composable
fun HomeScreen(
    uiState: ApertureUiState,
    onToggleConnection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        GlobeBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Aperture VPN",
                style = MaterialTheme.typography.headlineLarge,
                color = ApertureColors.Foreground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${uiState.profileCount} profiles cached",
                style = MaterialTheme.typography.bodyMedium,
                color = ApertureColors.Muted,
            )
            Spacer(modifier = Modifier.height(32.dp))

            val connected = uiState.connectionState == ConnectionState.CONNECTED
            val connecting = uiState.connectionState == ConnectionState.CONNECTING

            val buttonLabel = when (uiState.connectionState) {
                ConnectionState.CONNECTED -> "Disconnect"
                ConnectionState.CONNECTING -> "Connecting"
                ConnectionState.DISCONNECTING -> "Disconnecting"
                else -> "Connect"
            }

            Button(
                onClick = onToggleConnection,
                enabled = uiState.connectionState != ConnectionState.CONNECTING &&
                    uiState.connectionState != ConnectionState.DISCONNECTING,
                modifier = Modifier.size(160.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (connected) ApertureColors.Background else ApertureColors.Foreground,
                    contentColor = if (connected) ApertureColors.Foreground else ApertureColors.Background,
                ),
                border = BorderStroke(2.dp, ApertureColors.Foreground),
            ) {
                Text(text = buttonLabel, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(24.dp))
            val server = uiState.activeServer ?: uiState.rankedServers.firstOrNull()?.profile
            if (server != null) {
                val latency = uiState.rankedServers.firstOrNull {
                    it.profile.ip == server.ip
                }?.probe?.latencyMs
                val latencyLabel = latency?.let { "${it}ms" } ?: "n/a"
                Text(
                    text = "${server.countryLong} (${server.countryShort})",
                    color = ApertureColors.Foreground,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${server.ip} - $latencyLabel",
                    color = ApertureColors.Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (uiState.statusMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.statusMessage,
                    color = ApertureColors.Muted,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }

            if (connecting) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Trying ranked servers",
                    color = ApertureColors.Muted,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
