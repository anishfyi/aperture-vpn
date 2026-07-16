package io.github.anishfyi.aperture.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import io.github.anishfyi.aperture.R

@OptIn(ExperimentalTextApi::class)
private fun inter(weight: FontWeight) = FontFamily(
    Font(
        R.font.inter,
        weight = weight,
        variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
    ),
)

val Sans = inter(FontWeight.Normal)
val SansMedium = inter(FontWeight.Medium)
val SansBold = inter(FontWeight.Bold)

val ApertureTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = Sans),
        displayMedium = displayMedium.copy(fontFamily = Sans),
        displaySmall = displaySmall.copy(fontFamily = Sans),
        headlineLarge = headlineLarge.copy(fontFamily = SansBold),
        headlineMedium = headlineMedium.copy(fontFamily = SansBold),
        headlineSmall = headlineSmall.copy(fontFamily = SansBold),
        titleLarge = titleLarge.copy(fontFamily = SansMedium),
        titleMedium = titleMedium.copy(fontFamily = SansMedium),
        titleSmall = titleSmall.copy(fontFamily = SansMedium),
        bodyLarge = bodyLarge.copy(fontFamily = Sans),
        bodyMedium = bodyMedium.copy(fontFamily = Sans),
        bodySmall = bodySmall.copy(fontFamily = Sans),
        labelLarge = labelLarge.copy(fontFamily = SansMedium),
        labelMedium = labelMedium.copy(fontFamily = SansMedium),
        labelSmall = labelSmall.copy(fontFamily = SansMedium),
    )
}
