package com.alpriest.energystats.presentation

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class IconScale {
    LARGE,
    SMALL;

    fun frameHeight(): Dp {
        return iconHeight() + 5.dp
    }

    fun iconHeight(): Dp {
        return when (this) {
            LARGE -> {
                64.dp
            }

            SMALL -> {
                30.dp
            }
        }
    }

    fun strokeWidth(): Float {
        return when (this) {
            LARGE -> {
                6f
            }

            SMALL -> {
                2f
            }
        }
    }
}