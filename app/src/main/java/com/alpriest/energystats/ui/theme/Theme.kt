package com.alpriest.energystats.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.settings.ColorThemeMode

private val darkColorPalette = darkColorScheme(
    primary = TintColor,
    secondary = DarkSecondaryBackground,
    background = DarkBackground,
    onBackground = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.LightGray,
    surface = DarkHeader,
    onSurface = Color.White
)

private val lightColorPalette = lightColorScheme(
    primary = TintColor,
    secondary = SecondaryBackground,
    background = PaleWhite,
    onBackground = Color.DarkGray,
    onPrimary = Color.White,
    onSecondary = Color.DarkGray,
    surface = Color.White,
    onSurface = Color.Black,
)

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
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
        colorScheme = colors,
        typography = typography,
        content = content,
        shapes = shapes
    )
}
