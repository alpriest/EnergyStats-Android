package com.alpriest.energystats.ui.flow

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.models.kW
import com.alpriest.energystats.models.rounded
import com.alpriest.energystats.models.sameValueAs
import com.alpriest.energystats.models.w

@Composable
fun PowerFlowView(amount: Double, modifier: Modifier = Modifier) {
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

    Box(
        modifier = modifier
            .fillMaxHeight()
            .onGloballyPositioned { coordinates ->
                height = coordinates.size.height.toFloat()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier.fillMaxHeight()) {
            drawLine(
                color = Color.LightGray,
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, size.height),
                strokeWidth = 6f
            )

            if (isFlowing) {
                drawCircle(
                    color = Color.LightGray,
                    center = Offset(x = size.width / 2, y = offsetModifier(ballYPosition)),
                    radius = 12f
                )
            }
        }

        Text(
            text = if (asKw) {
                amount.kW()
            } else {
                amount.w()
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { asKw = !asKw }
        )
    }
}

@Preview
@Composable
fun PowerFlowViewPreview() {
    Row(Modifier.height(200.dp)) {
        PowerFlowView(
            3.0, modifier = Modifier
                .width(100.dp)
        )
        PowerFlowView(
            -3.0, modifier = Modifier
                .width(100.dp)
        )
    }
}