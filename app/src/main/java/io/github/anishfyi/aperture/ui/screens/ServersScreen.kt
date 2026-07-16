package io.github.anishfyi.aperture.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.anishfyi.aperture.data.RankedServer
import io.github.anishfyi.aperture.data.ServerProfile
import io.github.anishfyi.aperture.ui.ApertureUiState
import io.github.anishfyi.aperture.ui.theme.ApertureColors

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ServersScreen(
    uiState: ApertureUiState,
    onRefresh: () -> Unit,
    onConnect: (ServerProfile) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pullState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
    )

    Column(modifier = modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Back",
                modifier = Modifier.clickable(onClick = onBack),
                style = MaterialTheme.typography.titleMedium,
                color = ApertureColors.Muted,
            )
            Text(
                text = "Select location",
                style = MaterialTheme.typography.headlineMedium,
                color = ApertureColors.Foreground,
            )
        }
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullState),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(uiState.rankedServers, key = { it.profile.ip }) { ranked ->
                    ServerRow(ranked = ranked, onConnect = onConnect)
                    HorizontalDivider(color = ApertureColors.Muted.copy(alpha = 0.4f))
                }
            }
            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = ApertureColors.Foreground,
                backgroundColor = ApertureColors.Background,
            )
        }
    }
}

@Composable
private fun ServerRow(
    ranked: RankedServer,
    onConnect: (ServerProfile) -> Unit,
) {
    val latency = ranked.probe.latencyMs?.let { "${it}ms" } ?: "meta"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onConnect(ranked.profile) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "${ranked.profile.countryLong} (${ranked.profile.countryShort})",
            color = ApertureColors.Foreground,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = ranked.profile.ip,
            color = ApertureColors.Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = "Latency $latency - Score ${"%.2f".format(ranked.score)}",
            color = ApertureColors.Foreground,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
