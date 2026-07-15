package io.github.anishfyi.aperture.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.anishfyi.aperture.ui.theme.ApertureColors

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "About",
            style = MaterialTheme.typography.headlineMedium,
            color = ApertureColors.Foreground,
        )
        Text(
            text = "Aperture VPN aggregates 100+ free OpenVPN profiles from VPN Gate, " +
                "probes and ranks servers, and connects via embedded ics-openvpn.",
            color = ApertureColors.Foreground,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "DNS is forced to AdGuard (94.140.14.14 / 94.140.15.15) for ad and tracker blocking.",
            color = ApertureColors.Foreground,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "No accounts. No trackers. Open source under GPL-2.0-or-later.",
            color = ApertureColors.Foreground,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Servers are third-party VPN Gate volunteers with varying logging policies. " +
                "No uptime guarantee.",
            color = ApertureColors.Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = "Disable Android Private DNS if DNS does not route through the tunnel.",
            color = ApertureColors.Muted,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
