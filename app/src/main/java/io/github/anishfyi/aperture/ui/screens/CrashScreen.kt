package io.github.anishfyi.aperture.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.anishfyi.aperture.ui.theme.ApertureColors

/*
 * Shown once after a crash so the trace can be read or screenshotted on devices
 * where logcat is not available. Dismissing deletes the stored trace.
 */
@Composable
fun CrashScreen(
    trace: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Text(
            text = "Last crash",
            color = ApertureColors.Foreground,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Screenshot this and send it over so the crash can be fixed.",
            color = ApertureColors.Muted,
            fontSize = 13.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = trace,
            color = ApertureColors.Foreground,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, ApertureColors.Foreground),
        ) {
            Text(text = "Dismiss", color = ApertureColors.Foreground)
        }
    }
}
