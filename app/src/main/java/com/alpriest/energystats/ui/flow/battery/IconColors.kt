package com.alpriest.energystats.ui.flow.battery

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.ui.theme.IconColorInDarkTheme
import com.alpriest.energystats.ui.theme.IconColorInLightTheme

@Composable
fun iconBackgroundColor(isDarkMode: Boolean): Color {
    return if (isDarkMode) {
        IconColorInDarkTheme
    } else {
        IconColorInLightTheme
    }
}

@Composable
fun iconForegroundColor(isDarkMode: Boolean): Color {
    return if (isDarkMode) {
        IconColorInLightTheme
    } else {
        IconColorInDarkTheme
    }
}