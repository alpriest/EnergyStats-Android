package com.alpriest.energystats.shared.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PylonView(modifier: Modifier, color: Color, strokeWidth: Float) {
    Canvas(
        modifier = modifier.padding(top = 2.dp)
    ) {
        val hSize: Float = size.width * 0.1f
        val vSize: Float = size.height * 0.1f

        val leftLegBottom = Offset(x = hSize * 2.5f, y = size.height)
        val leftLegTop = Offset(x = hSize * 4, y = 0f - (strokeWidth / 2.0f))
        val rightLegBottom = Offset(x = hSize * 7.5f, y = size.height)
        val rightLegTop = Offset(x = hSize * 6.0f, y = 0f - (strokeWidth / 2.0f))
        val topBarInset = size.width / 100f

        drawLine(
            color = color,
            start = leftLegBottom,
            end = leftLegTop,
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = leftLegTop.minus(Offset(topBarInset, -1f)),
            end = rightLegTop.plus(Offset(topBarInset, 1f)),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = rightLegTop,
            end = rightLegBottom,
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(x = hSize * 1.5f, y = vSize * 2.5f),
            end = Offset(x = hSize * 8.5f, y = vSize * 2.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(x = 3f, y = vSize * 5f),
            end = Offset(x = size.width - 3f, y = vSize * 5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = leftLegBottom,
            end = Offset(x = hSize * 6.9f, y = vSize * 5f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = rightLegBottom,
            end = Offset(x = hSize * 3.1f, y = vSize * 5f),
            strokeWidth = strokeWidth
        )
    }
}

@Preview
@Composable
fun PylonViewPreviewWatch() {
    PylonView(
        modifier = Modifier
            .height(30.dp)
            .width(30.dp * 1.3f),
        Color.Yellow,
        4f
    )
}

@Preview
@Composable
fun PylonViewPreviewPhone() {
    PylonView(
        modifier = Modifier
            .height(60.dp)
            .width(60.dp * 1.3f),
        Color.Yellow,
        6f
    )
}
