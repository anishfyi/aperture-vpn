package io.github.anishfyi.aperture.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import io.github.anishfyi.aperture.R

@OptIn(ExperimentalTextApi::class)
private fun grotesk(weight: FontWeight) = FontFamily(
    Font(
        R.font.space_grotesk,
        weight = weight,
        variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
    ),
)

val Grotesk = grotesk(FontWeight.Normal)
val GroteskMedium = grotesk(FontWeight.Medium)
val GroteskBold = grotesk(FontWeight.Bold)

val ApertureTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = Grotesk),
        displayMedium = displayMedium.copy(fontFamily = Grotesk),
        displaySmall = displaySmall.copy(fontFamily = Grotesk),
        headlineLarge = headlineLarge.copy(fontFamily = GroteskBold),
        headlineMedium = headlineMedium.copy(fontFamily = GroteskBold),
        headlineSmall = headlineSmall.copy(fontFamily = GroteskBold),
        titleLarge = titleLarge.copy(fontFamily = GroteskMedium),
        titleMedium = titleMedium.copy(fontFamily = GroteskMedium),
        titleSmall = titleSmall.copy(fontFamily = GroteskMedium),
        bodyLarge = bodyLarge.copy(fontFamily = Grotesk),
        bodyMedium = bodyMedium.copy(fontFamily = Grotesk),
        bodySmall = bodySmall.copy(fontFamily = Grotesk),
        labelLarge = labelLarge.copy(fontFamily = GroteskMedium),
        labelMedium = labelMedium.copy(fontFamily = GroteskMedium),
        labelSmall = labelSmall.copy(fontFamily = GroteskMedium),
    )
}
