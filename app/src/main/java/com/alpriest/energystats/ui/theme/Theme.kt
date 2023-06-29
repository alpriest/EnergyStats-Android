package com.alpriest.energystats.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = TintColor,
    secondary = Teal200,
    background = DarkBackground,
    onBackground = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.LightGray,
    surface = DarkHeader
)

private val LightColorPalette = lightColors(
    primary = TintColor,
    secondary = Teal200,
    background = PaleWhite,
    onBackground = Color.DarkGray,
    onPrimary = Color.White,
    onSecondary = Color.DarkGray,
    surface = PaleHeader

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun EnergyStatsTheme(darkTheme: Boolean = isSystemInDarkTheme(), useLargeDisplay: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    val typography = if (useLargeDisplay) {
        LargeTypography
    } else {
        Typography
    }

    MaterialTheme(
        colors = colors,
        typography = typography,
        shapes = Shapes,
        content = content
    )
}
