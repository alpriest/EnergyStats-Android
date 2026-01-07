package com.alpriest.energystats.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme

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
                3f
            }
        }
    }

    @Composable
    fun line1TextStyle(): TextStyle {
        return when (this) {
            SMALL -> MaterialTheme.typography.caption2.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold)
            LARGE -> MaterialTheme.typography.body1.copy(fontSize = 24.sp)
        }
    }

    @Composable
    fun line2TextStyle(): TextStyle {
        return when (this) {
            SMALL -> MaterialTheme.typography.caption2.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
            LARGE -> MaterialTheme.typography.body1.copy(fontSize = 20.sp)
        }
    }
}