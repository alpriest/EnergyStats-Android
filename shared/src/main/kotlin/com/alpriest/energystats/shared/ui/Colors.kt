package com.alpriest.energystats.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Sunny = Color(0xFFFAD652)
val IconColorInDarkTheme = Color(0xFFFFFFFF)
val IconColorInLightTheme = Color(0xFF000000)

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