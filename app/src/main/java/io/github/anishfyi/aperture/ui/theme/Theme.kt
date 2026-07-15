package io.github.anishfyi.aperture.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Black = Color(0xFF000000)
private val White = Color(0xFFFFFFFF)
private val Gray = Color(0xFF888888)

private val ApertureColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = Gray,
    onSecondary = Black,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White,
    outline = White,
)

object ApertureColors {
    val Background = Black
    val Foreground = White
    val Muted = Gray
}

@Composable
fun ApertureTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ApertureColorScheme,
        content = content,
    )
}
