package com.alpriest.energystats.ui.flow

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.*
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow

enum class PowerFlowLinePosition {
    LEFT,
    MIDDLE,
    RIGHT,
    NONE,
    HORIZONTAL
}

@Composable
fun PowerFlowView(
    amount: Double,
    themeStream: MutableStateFlow<AppTheme>,
    position: PowerFlowLinePosition,
    modifier: Modifier = Modifier,
    useColouredLines: Boolean = false,
) {
    var height by remember { mutableStateOf(0f) }
    val isFlowing = !amount.rounded(2).sameValueAs(0.0)
    val theme by themeStream.collectAsState()
    val inverterColor = Color.LightGray
    val lineColor = if (isFlowing && useColouredLines && theme.useColouredLines) flowingColour(amount) else {
        Color.LightGray
    }
    val strokeWidth = theme.strokeWidth()

    val powerTextColor = if (isFlowing && useColouredLines && theme.useColouredLines) textForeground(amount) else {
        PowerFlowNeutralText
    }

    Box {
        VerticalLine(amount = amount, color = lineColor, powerTextColor = powerTextColor, modifier = Modifier, theme = theme)

        Canvas(
            modifier
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

//        modifier = modifier
//            .onGloballyPositioned { coordinates ->
//                height = coordinates.size.height.toFloat()
//            },
//        contentAlignment = Alignment.Center
//    ) {
//        Canvas(
//            modifier
//                .fillMaxHeight()
//                .fillMaxWidth()
//        ) {
//            if (position != PowerFlowLinePosition.HORIZONTAL) {
//                drawLine(
//                    color = verticalLineColor,
//                    start = Offset(size.width / 2, 0f),
//                    end = Offset(size.width / 2, size.height),
//                    strokeWidth = strokeWidth
//                )
//            }
//
//            when (position) {
//                PowerFlowLinePosition.LEFT -> {
//                    drawLine(
//                        color = inverterColor,
//                        start = Offset(size.width / 2 - (strokeWidth / 2), strokeWidth / 2),
//                        end = Offset(size.width, strokeWidth / 2),
//                        strokeWidth = strokeWidth
//                    )
//                }
//
//                PowerFlowLinePosition.MIDDLE -> {
//                    drawLine(
//                        color = inverterColor,
//                        start = Offset(0f, strokeWidth / 2),
//                        end = Offset(size.width, strokeWidth / 2),
//                        strokeWidth = strokeWidth
//                    )
//                }
//
//                PowerFlowLinePosition.RIGHT -> {
//                    drawLine(
//                        color = inverterColor,
//                        start = Offset(size.width / 2 + (strokeWidth / 2), strokeWidth / 2),
//                        end = Offset(0f, strokeWidth / 2),
//                        strokeWidth = strokeWidth
//                    )
//                }
//
//                PowerFlowLinePosition.HORIZONTAL -> {
//                    drawLine(
//                        color = verticalLineColor,
//                        start = Offset(0f, size.height * 0.7f),
//                        end = Offset(size.width, size.height * 0.7f),
//                        strokeWidth = strokeWidth
//                    )
//                }
//
//                PowerFlowLinePosition.NONE -> {}
//            }
//        }
//
//        if (isFlowing) {
//            Canvas(
//                modifier = modifier
//                    .fillMaxHeight()
//                    .fillMaxWidth()
//            ) {
//                drawCircle(
//                    color = verticalLineColor,
//                    center = Offset(x = size.width / 2, y = offsetModifier(ballYPosition)),
//                    radius = strokeWidth * 2.0f
//                )
//            }
//
//            Card(
//                shape = RoundedCornerShape(4.dp),
//                colors = cardColors(containerColor = verticalLineColor)
//            ) {
//                Text(
//                    text = amount.energy(theme.displayUnit, theme.decimalPlaces),
//                    color = powerTextColor,
//                    fontWeight = FontWeight.Bold,
//                    fontSize = fontSize,
//                    modifier = Modifier
//                        .padding(vertical = 1.dp)
//                        .padding(horizontal = 3.dp)
//                )
//            }
//        }
//    }
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
            position = PowerFlowLinePosition.LEFT
        )
        PowerFlowView(
            5.255,
            themeStream = MutableStateFlow(AppTheme.preview(useLargeDisplay = true, showBatteryTemperature = true)),
            position = PowerFlowLinePosition.MIDDLE
        )
        PowerFlowView(
            -3.0,
            themeStream = MutableStateFlow(AppTheme.preview(useLargeDisplay = false, showBatteryTemperature = false)),
            position = PowerFlowLinePosition.RIGHT
        )
    }
}