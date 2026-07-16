package io.github.anishfyi.aperture.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import io.github.anishfyi.aperture.ui.theme.ApertureColors

/*
 * First-run / load screen. Shown while the profile list is fetched and probed
 * so the home screen only appears once servers are ready.
 */
@Composable
fun SetupScreen(
    statusLine: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "APRTR",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.32.em,
            color = ApertureColors.Foreground,
        )
        Spacer(modifier = Modifier.height(40.dp))
        LoadingArc()
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = statusLine,
            color = ApertureColors.Muted,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun LoadingArc() {
    val transition = rememberInfiniteTransition(label = "setup")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sweep",
    )
    Canvas(modifier = Modifier.size(40.dp)) {
        val stroke = 3f
        drawCircle(
            color = ApertureColors.Muted,
            radius = size.minDimension / 2 - stroke,
            center = Offset(size.width / 2, size.height / 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
        )
        drawArc(
            color = ApertureColors.Foreground,
            startAngle = angle,
            sweepAngle = 90f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}
