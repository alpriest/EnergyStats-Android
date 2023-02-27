package com.alpriest.energystats.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AppTheme {
    UseLargeDisplay,
    UseDefaultDisplay;

    fun fontSize(): TextUnit {
        return when (this) {
            UseDefaultDisplay -> 16.sp
            UseLargeDisplay -> 26.sp
        }
    }

    fun strokeWidth(): Float {
        return when (this) {
            UseDefaultDisplay -> 6f
            UseLargeDisplay -> 12f
        }
    }

    fun iconHeight(): Dp {
        return when (this) {
            UseDefaultDisplay -> 40.dp
            UseLargeDisplay -> 80.dp
        }
    }
}
