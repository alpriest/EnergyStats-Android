package com.alpriest.energystats.ui.flow.NewPowerFlowView

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import kotlin.math.max

@Composable
fun NewLine(amount: Double, modifier: Modifier) {
    val phaseAnimation = rememberInfiniteTransition()
    val pathPhase by phaseAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                max(
                    200,
                    3000 - abs(amount * 1000.0).toInt()
                ), easing = LinearEasing
            )
        ),
        label = "ball y position"
    )
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), phase = pathPhase)

    Column {
        Canvas(modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()) {
            drawLine(
                color = Color.Red,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                pathEffect = pathEffect,
                strokeWidth = 5.0f
            )
        }
    }
}

@Composable
@Preview(heightDp = 200, widthDp = 200)
fun NewLinePreview() {
    NewLine(amount = 2.0, modifier = Modifier)
}