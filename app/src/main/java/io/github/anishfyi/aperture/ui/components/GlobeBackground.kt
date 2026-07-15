package io.github.anishfyi.aperture.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import io.github.anishfyi.aperture.ui.theme.ApertureColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GlobeBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "globe")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val radius = size.minDimension * 0.35f
        val center = Offset(size.width / 2f, size.height / 2f)
        val stroke = Stroke(width = 2f, cap = StrokeCap.Round)

        drawCircle(
            color = ApertureColors.Foreground.copy(alpha = 0.25f),
            radius = radius,
            center = center,
            style = stroke,
        )

        rotate(rotation, center) {
            for (i in -2..2) {
                val y = center.y + i * radius * 0.35f
                val span = kotlin.math.sqrt((radius * radius - (y - center.y) * (y - center.y)).coerceAtLeast(0f))
                drawLine(
                    color = ApertureColors.Foreground.copy(alpha = 0.18f),
                    start = Offset(center.x - span, y),
                    end = Offset(center.x + span, y),
                    strokeWidth = 1.5f,
                )
            }

            for (angle in 0 until 360 step 30) {
                val rad = Math.toRadians(angle.toDouble())
                val xScale = cos(rad).toFloat().coerceAtLeast(0.15f)
                drawCircle(
                    color = Color.Transparent,
                    radius = radius * xScale,
                    center = center,
                    style = Stroke(
                        width = 1.2f,
                        cap = StrokeCap.Round,
                    ),
                )
            }
        }

        for (dotAngle in 0 until 360 step 24) {
            val rad = Math.toRadians((dotAngle + rotation * 0.4f).toDouble())
            val dotRadius = radius * 0.92f
            val dot = Offset(
                center.x + dotRadius * cos(rad).toFloat(),
                center.y + dotRadius * sin(rad).toFloat() * 0.55f,
            )
            drawCircle(
                color = ApertureColors.Foreground.copy(alpha = 0.35f),
                radius = 2.5f,
                center = dot,
            )
        }
    }
}
