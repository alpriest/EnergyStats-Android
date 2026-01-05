package com.alpriest.energystats.shared.ui

import androidx.compose.foundation.Canvas
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

@Composable
fun BatteryView(modifier: Modifier = Modifier, foregroundColor: Color, backgroundColor: Color) {
    Canvas(
        modifier = modifier
    ) {
        val boxTop = size.height * 0.11f
        val terminalInset = size.width * 0.2f
        val terminalWidth = size.width * 0.2f
        val barHeight = size.height / 16f
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


@Preview
@Composable
fun BatteryViewPreviewWatch() {
    BatteryView(
        modifier = Modifier
            .height(20.dp)
            .width(20.dp * 1.25f),
        Color.Black,
        Color.White
    )
}

@Preview
@Composable
fun BatteryViewPreviewPhone() {
    BatteryView(
        modifier = Modifier
            .height(60.dp)
            .width(60.dp * 1.25f),
        Color.Black,
        Color.White
    )
}
