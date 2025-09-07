package com.alpriest.energystats.ui.helpers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun darkenColor(color: Color, percentage: Float): Color {
    val argb = color.toArgb()
    val alpha = argb ushr 24
    val red = argb shr 16 and 0xFF
    val green = argb shr 8 and 0xFF
    val blue = argb and 0xFF

    val darkenedRed = (red * (1 - percentage)).toInt()
    val darkenedGreen = (green * (1 - percentage)).toInt()
    val darkenedBlue = (blue * (1 - percentage)).toInt()

    return Color(alpha = alpha, red = darkenedRed, green = darkenedGreen, blue = darkenedBlue)
}