package com.alpriest.energystats.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.shared.models.ColorThemeMode

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

val themeCornerRadius = 4.dp

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(themeCornerRadius),
    medium = RoundedCornerShape(themeCornerRadius),
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
        shapes = shapes,
        content = content
    )
}

@Composable
fun ESButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    ) {
        content()
    }
}

@Composable
fun OutlinedESButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    colors: ButtonColors = ButtonDefaults.buttonColors().copy(
        containerColor = Color.Transparent,
        contentColor = colorScheme.primary
    ),
    content: @Composable RowScope.() -> Unit
) {
    ESButton(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        border = BorderStroke(
            width = 1.dp,
            brush = SolidColor(colorScheme.primary)
        ),
        contentPadding = contentPadding,
        content = content
    )
}