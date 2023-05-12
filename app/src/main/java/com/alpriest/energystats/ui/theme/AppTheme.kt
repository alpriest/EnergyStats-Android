package com.alpriest.energystats.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AppTheme(
    val useLargeDisplay: Boolean,
    val useColouredLines: Boolean,
    val showBatteryTemperature: Boolean,
    val showBatteryEstimate: Boolean,
    val decimalPlaces: Int,
    val showSunnyBackground: Boolean,
    val showUsableBatteryOnly: Boolean,
    val showTotalYield: Boolean
) {
    fun fontSize(): TextUnit {
        return when (useLargeDisplay) {
            false -> 16.sp
            true -> 26.sp
        }
    }

    fun strokeWidth(): Float {
        return when (useLargeDisplay) {
            false -> 6f
            true -> 12f
        }
    }

    fun iconHeight(): Dp {
        return when (useLargeDisplay) {
            false -> 40.dp
            true -> 80.dp
        }
    }

    companion object {}
}
