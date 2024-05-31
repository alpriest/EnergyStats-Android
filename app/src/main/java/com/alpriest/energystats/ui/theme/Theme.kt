package com.alpriest.energystats.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.settings.ColorThemeMode

private val darkColorPalette = darkColors(
    primary = TintColor,
    secondary = DarkSecondaryBackground,
    background = DarkBackground,
    onBackground = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.LightGray,
    surface = DarkHeader,
    onSurface = Color.White,
    primaryVariant = Color.DarkGray
)

private val lightColorPalette = lightColors(
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
    val colors = if (isDarkMode(colorThemeMode)) {
        darkColorPalette
    } else {
        lightColorPalette
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
