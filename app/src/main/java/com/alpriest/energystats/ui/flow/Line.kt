package com.alpriest.energystats.ui.flow

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.preview
import kotlin.math.abs
import kotlin.math.max

@Composable
fun Line(amount: Double, color: Color, modifier: Modifier, theme: AppTheme, orientation: LineOrientation, isFlowing: Boolean) {
    val phaseAnimation = rememberInfiniteTransition()
    val strokeWidth: Float = theme.strokeWidth()
    val initialValue = if (amount > 0) {
        40f
    } else {
        0f
    }
    val targetValue = if (amount > 0) {
        0f
    } else {
        40f
    }
    val pathPhase by phaseAnimation.animateFloat(
        initialValue = initialValue,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(
                max(
                    200,
                    2300 - abs(amount * 1000.0).toInt()
                ), easing = LinearEasing
            )
        ),
        label = "line phase"
    )
    val dashedPathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), phase = pathPhase)
    val solidPathEffect = PathEffect.dashPathEffect(floatArrayOf(0f, 0f))

    Column {
        Box(contentAlignment = Alignment.Center) {
            Canvas(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                when (orientation) {
                    LineOrientation.VERTICAL ->
                        drawLine(
                            color = color,
                            start = Offset(size.width / 2.0f, 0f),
                            end = Offset(size.width / 2.0f, size.height),
                            pathEffect = if (isFlowing) dashedPathEffect else solidPathEffect,
                            strokeWidth = strokeWidth
                        )

                    LineOrientation.HORIZONTAL ->
                        drawLine(
                            color = color,
                            start = Offset(0f, size.height / 2.0f),
                            end = Offset(size.width, size.height / 2.0f),
                            pathEffect = if (isFlowing) dashedPathEffect else solidPathEffect,
                            strokeWidth = 8.0f
                        )
                }
            }
        }
    }
}

@Composable
@Preview(heightDp = 200, widthDp = 200)
fun LinePreview() {
    Box {
        Line(
            amount = 2.0,
            color = Color.Red,
            modifier = Modifier,
            theme = AppTheme.preview(),
            orientation = LineOrientation.HORIZONTAL,
            isFlowing = true
        )
        Line(
            amount = 2.0,
            color = Color.Red,
            modifier = Modifier,
            theme = AppTheme.preview(),
            orientation = LineOrientation.VERTICAL,
            isFlowing = true
        )
    }
}