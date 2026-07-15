package io.github.anishfyi.aperture.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import io.github.anishfyi.aperture.R
import io.github.anishfyi.aperture.ui.ApertureViewModel
import io.github.anishfyi.aperture.ui.screens.AboutScreen
import io.github.anishfyi.aperture.ui.screens.HomeScreen
import io.github.anishfyi.aperture.ui.screens.ServersScreen
import io.github.anishfyi.aperture.ui.theme.ApertureColors

private data class Tab(val label: String, val iconRes: Int)

@Composable
fun ApertureNavHost(viewModel: ApertureViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf(
        Tab("Home", R.drawable.ic_tab_home),
        Tab("Servers", R.drawable.ic_tab_servers),
        Tab("About", R.drawable.ic_tab_about),
    )
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = ApertureColors.Background,
        bottomBar = {
            NavigationBar(containerColor = ApertureColors.Background) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                painter = painterResource(tab.iconRes),
                                contentDescription = tab.label,
                                tint = if (selectedTab == index) {
                                    ApertureColors.Foreground
                                } else {
                                    ApertureColors.Muted
                                },
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                color = if (selectedTab == index) {
                                    ApertureColors.Foreground
                                } else {
                                    ApertureColors.Muted
                                },
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        when (selectedTab) {
            0 -> HomeScreen(
                uiState = uiState,
                onToggleConnection = viewModel::toggleConnection,
                modifier = Modifier.padding(padding),
            )
            1 -> ServersScreen(
                uiState = uiState,
                onRefresh = { viewModel.refresh(force = true) },
                onConnect = viewModel::connectTo,
                modifier = Modifier.padding(padding),
            )
            else -> AboutScreen(modifier = Modifier.padding(padding))
        }
    }
}
