package com.alpriest.energystats.ui.flow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun PylonView(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.padding(top = 2.dp)
    ) {
        val hSize: Float = size.width * 0.1f
        val vSize: Float = size.height * 0.1f
        val strokeWidth = 8f
        val color = Color.Black

        val leftLegBottom = Offset(x = hSize * 2.5f, y = size.height)
        val leftLegTop = Offset(x = hSize * 4, y = 0f)
        val rightLegBottom = Offset(x = hSize * 7.5f, y = size.height)
        val rightLegTop = Offset(x = hSize * 6.0f, y = 0f)

        drawLine(
            color = color,
            start = leftLegBottom,
            end = leftLegTop,
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = leftLegTop,
            end = rightLegTop,
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
            start = Offset(x = hSize * 1f, y = vSize * 2.5f),
            end = Offset(x = hSize * 9, y = vSize * 2.5f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(x = 0f, y = vSize * 5f),
            end = Offset(x = size.width, y = vSize * 5f),
            strokeWidth = strokeWidth
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

@Preview(showBackground = true)
@Composable
fun PylonViewPreview() {
    val pylonHeight = 40.dp

    EnergyStatsTheme {
        Box(
            Modifier
                .height(pylonHeight)
                .width(pylonHeight * 1.2f)
        ) {
            PylonView(
                modifier = Modifier
                    .height(pylonHeight)
                    .width(pylonHeight * 0.9f)
            )
        }
    }
}