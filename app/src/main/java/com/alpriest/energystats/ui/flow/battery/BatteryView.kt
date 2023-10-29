package com.alpriest.energystats.ui.flow.battery

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.IconColorInDarkTheme
import com.alpriest.energystats.ui.theme.IconColorInLightTheme

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

@Composable
fun BatteryView(modifier: Modifier = Modifier, isDarkMode: Boolean) {
    val foregroundColor = iconForegroundColor(isDarkMode)
    val backgroundColor = iconBackgroundColor(isDarkMode)

    Canvas(
        modifier = modifier
    ) {
        val boxTop = size.height * 0.11f
        val terminalInset = size.width * 0.2f
        val terminalWidth = size.width * 0.2f
        val barHeight = 8f
        val halfBarHeight = barHeight / 2f

        // Battery
        val batterySize = Size(size.width, size.height - boxTop)
        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset(x = 0f, y = boxTop),
            size = batterySize,
            cornerRadius = CornerRadius(x = 10f, y = 10f)
        )

        // Negative terminal
        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset(x = terminalInset, y = 0f),
            size = Size(terminalWidth, boxTop + barHeight),
            cornerRadius = CornerRadius(x = 5f, y = 5f)
        )

        // Positive terminal
        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset(x = size.width - 2 * terminalInset, y = 0f),
            size = Size(terminalWidth, boxTop + barHeight),
            cornerRadius = CornerRadius(x = 5f, y = 5f)
        )

        // Minus
        drawRoundRect(
            color = foregroundColor,
            topLeft = Offset(x = terminalInset, y = boxTop + batterySize.height / 2.0f),
            size = Size(terminalWidth, barHeight),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Plus
        drawRoundRect(
            color = foregroundColor,
            topLeft = Offset(
                x = size.width - 2 * terminalInset,
                y = boxTop + batterySize.height / 2.0f
            ),
            size = Size(terminalWidth, barHeight),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = foregroundColor,
            topLeft = Offset(
                x = size.width - 2 * terminalInset + (terminalInset / 2f) - halfBarHeight,
                y = boxTop + batterySize.height / 2.0f - (terminalInset / 2f) + halfBarHeight
            ),
            size = Size(barHeight, terminalWidth),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BatteryViewPreview() {
    val height = 45.dp

    EnergyStatsTheme {
        Box {
            BatteryView(
                modifier = Modifier
                    .height(height)
                    .width(height * 1.25f),
                isDarkMode = true
            )
        }
    }
}