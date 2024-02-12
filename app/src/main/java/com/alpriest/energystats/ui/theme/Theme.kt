package com.alpriest.energystats.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.ui.settings.ColorThemeMode

private val DarkColorPalette = darkColors(
    primary = TintColor,
    secondary = SecondaryBackground,
    background = DarkBackground,
    onBackground = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.LightGray,
    surface = DarkHeader,
    onSurface = Color.White,
    primaryVariant = Color.DarkGray
)

private val LightColorPalette = lightColors(
    primary = TintColor,
    secondary = SecondaryBackground,
    background = PaleWhite,
    onBackground = Color.DarkGray,
    onPrimary = Color.White,
    onSecondary = Color.DarkGray,
    surface = Color.White,
    onSurface = Color.Black,
    primaryVariant = Color.LightGray
)

@Composable
fun EnergyStatsTheme(useLargeDisplay: Boolean = false, colorThemeMode: ColorThemeMode = ColorThemeMode.Auto, content: @Composable () -> Unit) {
    val darkTheme = when (colorThemeMode) {
        ColorThemeMode.Auto -> isSystemInDarkTheme()
        ColorThemeMode.Light -> false
        ColorThemeMode.Dark -> true
    }
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
