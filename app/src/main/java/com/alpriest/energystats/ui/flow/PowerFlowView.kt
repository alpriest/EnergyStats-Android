package com.alpriest.energystats.ui.flow

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.*
import com.alpriest.energystats.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow

enum class PowerFlowLinePosition {
    LEFT,
    MIDDLE,
    RIGHT,
    NONE,
    HORIZONTAL
}

enum class LineOrientation {
    VERTICAL,
    HORIZONTAL
}

@Composable
fun PowerText(amount: Double, themeStream: MutableStateFlow<AppTheme>, backgroundColor: Color, textColor: Color) {
    val theme by themeStream.collectAsState()
    val fontSize: TextUnit = theme.fontSize()

    Card(
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Text(
            text = amount.power(theme.displayUnit, theme.decimalPlaces),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            modifier = Modifier
                .padding(vertical = 1.dp)
                .padding(horizontal = 3.dp)
        )
    }
}

@Composable
fun PowerFlowView(
    amount: Double,
    themeStream: MutableStateFlow<AppTheme>,
    position: PowerFlowLinePosition,
    modifier: Modifier = Modifier,
    useColouredLines: Boolean = false,
    orientation: LineOrientation
) {
    val theme by themeStream.collectAsState()
    val inverterColor = Color.LightGray
    val lineColor = if (amount.isFlowing() && useColouredLines && theme.useColouredLines) flowingColour(amount) else {
        Color.LightGray
    }
    val strokeWidth = theme.strokeWidth()

    val powerTextColor = if (amount.isFlowing() && useColouredLines && theme.useColouredLines) textForeground(amount) else {
        PowerFlowNeutralText
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Line(amount, lineColor, Modifier, theme, orientation, amount.isFlowing())

        if (amount.isFlowing()) {
            PowerText(amount, themeStream, lineColor, powerTextColor)
        }

        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            when (position) {
                PowerFlowLinePosition.LEFT -> {
                    drawLine(
                        color = inverterColor,
                        start = Offset(size.width / 2 - (strokeWidth / 2), strokeWidth / 2),
                        end = Offset(size.width, strokeWidth / 2),
                        strokeWidth = strokeWidth
                    )
                }

                PowerFlowLinePosition.MIDDLE -> {
                    drawLine(
                        color = inverterColor,
                        start = Offset(0f, strokeWidth / 2),
                        end = Offset(size.width, strokeWidth / 2),
                        strokeWidth = strokeWidth
                    )
                }

                PowerFlowLinePosition.RIGHT -> {
                    drawLine(
                        color = inverterColor,
                        start = Offset(size.width / 2 + (strokeWidth / 2), strokeWidth / 2),
                        end = Offset(0f, strokeWidth / 2),
                        strokeWidth = strokeWidth
                    )
                }

                PowerFlowLinePosition.HORIZONTAL -> {
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, size.height * 0.7f),
                        end = Offset(size.width, size.height * 0.7f),
                        strokeWidth = strokeWidth
                    )
                }

                PowerFlowLinePosition.NONE -> {}
            }
        }
    }
}

fun flowingColour(amount: Double): Color {
    return if (amount < 0) {
        PowerFlowNegative
    } else {
        PowerFlowPositive
    }
}

fun textForeground(amount: Double): Color {
    return if (amount < 0) {
        PowerFlowNegativeText
    } else {
        PowerFlowPositiveText
    }
}

@Preview
@Composable
fun PowerFlowViewPreview() {
    Row(Modifier.height(200.dp)) {
        PowerFlowView(
            5.255,
            themeStream = MutableStateFlow(AppTheme.preview()),
            position = PowerFlowLinePosition.LEFT,
            orientation = LineOrientation.VERTICAL
        )
        PowerFlowView(
            5.255,
            themeStream = MutableStateFlow(AppTheme.preview(useLargeDisplay = true, showBatteryTemperature = true)),
            position = PowerFlowLinePosition.MIDDLE,
            orientation = LineOrientation.VERTICAL
        )
        PowerFlowView(
            -3.0,
            themeStream = MutableStateFlow(AppTheme.preview(useLargeDisplay = false, showBatteryTemperature = false)),
            position = PowerFlowLinePosition.RIGHT,
            orientation = LineOrientation.VERTICAL
        )
    }
}