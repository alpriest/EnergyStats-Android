package com.alpriest.energystats.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray

val Sunny = Color(0xFFFAD652)
val IconColorInDarkTheme = Color(0xFFFFFFFF)
val IconColorInLightTheme = Color(0xFF000000)
val PaleWhite = Color(0xFFFFFFFF)
val DarkBackground = Color(0xFF161616)
val DarkHeader = Color(0xFF252525)
val DimmedTextColor = Color(190, 190, 190)
val TintColor = Color(52, 120, 247)
val SecondaryBackground = Color(0xFFC2C2C2)
val DarkSecondaryBackground = Color(0xFF535353)
val Green = Color(0xFF30A530)
val Red = Color(0xFFA72A2A)
val PowerFlowPositive = Green
val PowerFlowNegative = Red
val PowerFlowNeutral = LightGray
val PowerFlowPositiveText = Color.Companion.White
val PowerFlowNegativeText = Color.Companion.White
val PowerFlowNeutralText = Color.Companion.Black
val LightApproximationHeader = Color(0xFF509DB3)
val LightApproximationBackground = Color(0xFFECF7F9)
val ApproximationHeaderText = Color.Companion.White
val DarkApproximationHeader = Color(0xFF2A7185)
val DarkApproximationBackground = Color.Companion.Black
val WebLinkColorInDarkTheme = Color(0xFFAAAAFF)
val WebLinkColorInLightTheme = TintColor
val Orange = Color(0xFFFFA500)
val MarkerLineInDarkTheme = Color(0xffffffff)
val MarkerLineInLightTheme = Color(0xFFF44336)

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