package com.alpriest.energystats.ui.helpers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.shared.ui.MarkerLineInDarkTheme
import com.alpriest.energystats.shared.ui.MarkerLineInLightTheme

@Composable
fun lineMarkerColor(isDarkMode: Boolean): Color {
    return if (isDarkMode) {
        MarkerLineInDarkTheme
    } else {
        MarkerLineInLightTheme
    }
}

@Composable
fun axisLabelColor(isDarkMode: Boolean): Color {
    return if (isDarkMode) {
        Color.White
    } else {
        Color.Black
    }
}