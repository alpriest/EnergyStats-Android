package com.alpriest.energystats.ui.helpers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.ui.theme.MarkerLineInDarkTheme
import com.alpriest.energystats.ui.theme.MarkerLineInLightTheme

@Composable
fun lineMarkerColor(isDarkMode: Boolean): Color {
    return if (isDarkMode) {
        MarkerLineInDarkTheme
    } else {
        MarkerLineInLightTheme
    }
}