package com.alpriest.energystats.ui.statsgraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.shared.ui.IconColorInDarkTheme
import com.alpriest.energystats.shared.ui.IconColorInLightTheme

@Composable
fun selfSufficiencyLineColor(isDarkMode: Boolean): Color {
    return if (isDarkMode) {
        IconColorInDarkTheme
    } else {
        IconColorInLightTheme
    }
}