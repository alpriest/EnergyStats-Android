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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.theme.AppTheme
import kotlin.math.abs
import kotlin.math.max

@Composable
fun VerticalLine(amount: Double, color: Color, modifier: Modifier, powerTextColor: Color, theme: AppTheme) {
    val phaseAnimation = rememberInfiniteTransition()
    val strokeWidth: Float = theme.strokeWidth()
    val fontSize: TextUnit = theme.fontSize()
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
                    2000 - abs(amount * 1000.0).toInt()
                ), easing = LinearEasing
            )
        ),
        label = "ball y position"
    )
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), phase = pathPhase)

    Column {
        Box(contentAlignment = Alignment.Center) {
            Canvas(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                drawLine(
                    color = color,
                    start = Offset(size.width / 2.0f, 0f),
                    end = Offset(size.width / 2.0f, size.height),
                    pathEffect = pathEffect,
                    strokeWidth = strokeWidth
                )
            }

            Card(
                shape = RoundedCornerShape(4.dp),
                colors = cardColors(containerColor = color)
            ) {
                Text(
                    text = amount.energy(theme.displayUnit, theme.decimalPlaces),
                    color = powerTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    modifier = Modifier
                        .padding(vertical = 1.dp)
                        .padding(horizontal = 3.dp)
                )
            }

        }
    }
}

@Composable
fun HorizontalLine(amount: Double, color: Color, modifier: Modifier) {
    val phaseAnimation = rememberInfiniteTransition()
    val pathPhase by phaseAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                max(
                    200,
                    2000 - abs(amount * 1000.0).toInt()
                ), easing = LinearEasing
            )
        ),
        label = "ball y position"
    )
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), phase = pathPhase)

    Column {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            drawLine(
                color = color,
                start = Offset(0f, size.height / 2.0f),
                end = Offset(size.width, size.height / 2.0f),
                pathEffect = pathEffect,
                strokeWidth = 8.0f
            )
        }
    }
}

@Composable
@Preview(heightDp = 200, widthDp = 200)
fun NewLinePreview() {
    Box {
        HorizontalLine(amount = 2.0, color = Color.Green, modifier = Modifier)
        VerticalLine(amount = 2.0, color = Color.Red, modifier = Modifier, powerTextColor = Color.Red, theme = AppTheme.preview())
    }
}