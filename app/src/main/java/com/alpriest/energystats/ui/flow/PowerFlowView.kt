package com.alpriest.energystats.ui.flow

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.kW
import com.alpriest.energystats.models.rounded
import com.alpriest.energystats.models.sameValueAs
import com.alpriest.energystats.models.w
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.PowerFlowNegative
import com.alpriest.energystats.ui.theme.PowerFlowPositive
import kotlinx.coroutines.flow.MutableStateFlow

enum class PowerFlowLinePosition {
    LEFT,
    MIDDLE,
    RIGHT,
    NONE
}

@Composable
fun PowerFlowView(
    amount: Double,
    themeStream: MutableStateFlow<AppTheme>,
    position: PowerFlowLinePosition,
    useColouredLines: Boolean = false,
    modifier: Modifier = Modifier
) {
    var asKw by remember { mutableStateOf(true) }
    var height by remember { mutableStateOf(0f) }
    val phaseAnimation = rememberInfiniteTransition()
    val offsetModifier: (Float) -> Float = {
        if (amount > 0f) {
            it
        } else {
            height - it
        }
    }
    val ballYPosition by phaseAnimation.animateFloat(
        initialValue = 0f,
        targetValue = height,
        animationSpec = infiniteRepeatable(
            animation = tween(
                kotlin.math.max(
                    400,
                    7000 - kotlin.math.abs(amount * 1000.0).toInt()
                ), easing = LinearEasing
            )
        )
    )
    val isFlowing = !amount.rounded(2).sameValueAs(0.0)
    val theme by themeStream.collectAsState()
    val strokeWidth: Float = theme.strokeWidth()
    val fontSize: TextUnit = theme.fontSize()
    val inverterColor = Color.LightGray
    val verticalLineColor = if (isFlowing && useColouredLines && theme.useColouredLines) flowingColour(amount) else {
        Color.LightGray
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .onGloballyPositioned { coordinates ->
                height = coordinates.size.height.toFloat()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            drawLine(
                color = verticalLineColor,
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, size.height),
                strokeWidth = strokeWidth
            )

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
                PowerFlowLinePosition.NONE -> {}
            }

            if (isFlowing) {
                drawCircle(
                    color = verticalLineColor,
                    center = Offset(x = size.width / 2, y = offsetModifier(ballYPosition)),
                    radius = strokeWidth * 2.0f
                )
            }
        }

        Box(modifier = Modifier.background(colors.background)) {
            Text(
                text = if (asKw) {
                    amount.kW()
                } else {
                    amount.w()
                },
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                modifier = Modifier
                    .clickable { asKw = !asKw }
                    .padding(1.dp)
            )
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

@Preview
@Composable
fun PowerFlowViewPreview() {
    Row(Modifier.height(200.dp)) {
        PowerFlowView(
            5.255,
            themeStream = MutableStateFlow(AppTheme(useLargeDisplay = true, useColouredLines = true, showBatteryTemperature = true)),
            position = PowerFlowLinePosition.LEFT
        )
        PowerFlowView(
            5.255,
            themeStream = MutableStateFlow(AppTheme(useLargeDisplay = true, useColouredLines = true, showBatteryTemperature = true)),
            position = PowerFlowLinePosition.MIDDLE
        )
        PowerFlowView(
            -3.0,
            themeStream = MutableStateFlow(AppTheme(useLargeDisplay = false, useColouredLines = true, showBatteryTemperature = false)),
            position = PowerFlowLinePosition.RIGHT
        )
    }
}