package io.github.anishfyi.aperture.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.anishfyi.aperture.data.ConnectionState
import io.github.anishfyi.aperture.ui.ApertureUiState
import io.github.anishfyi.aperture.ui.theme.ApertureColors

@Composable
fun HomeScreen(
    uiState: ApertureUiState,
    onToggleConnection: () -> Unit,
    onSelectLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = io.github.anishfyi.aperture.R.drawable.ic_aprtr_mark),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "APRTR",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.32.em,
            color = ApertureColors.Foreground,
        )
        Spacer(modifier = Modifier.height(48.dp))

        val connected = uiState.connectionState == ConnectionState.CONNECTED
        val busy = uiState.connectionState == ConnectionState.CONNECTING ||
            uiState.connectionState == ConnectionState.DISCONNECTING

        val buttonLabel = when (uiState.connectionState) {
            ConnectionState.CONNECTED -> "Disconnect"
            ConnectionState.CONNECTING -> "Connecting"
            ConnectionState.DISCONNECTING -> "Stopping"
            else -> "Connect"
        }

        Button(
            onClick = onToggleConnection,
            enabled = !busy,
            modifier = Modifier.size(170.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (connected) ApertureColors.Background else ApertureColors.Foreground,
                contentColor = if (connected) ApertureColors.Foreground else ApertureColors.Background,
                disabledContainerColor = ApertureColors.Background,
                disabledContentColor = ApertureColors.Muted,
            ),
            border = BorderStroke(2.dp, if (busy) ApertureColors.Muted else ApertureColors.Foreground),
        ) {
            Text(text = buttonLabel, textAlign = TextAlign.Center, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onSelectLocation,
            modifier = Modifier.fillMaxWidth(0.7f),
            border = BorderStroke(1.dp, ApertureColors.Foreground),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ApertureColors.Foreground,
            ),
        ) {
            Text(text = "Select location")
        }

        Spacer(modifier = Modifier.height(28.dp))

        val server = uiState.activeServer
        if (server != null) {
            Text(
                text = "${server.countryLong} (${server.countryShort})",
                color = ApertureColors.Foreground,
                style = MaterialTheme.typography.titleMedium,
            )
        } else {
            Text(
                text = "${uiState.profileCount} servers available",
                color = ApertureColors.Muted,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (uiState.statusMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = uiState.statusMessage,
                color = ApertureColors.Muted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}
