package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.diagonalLinesIf(
    enabled: Boolean,
    lineColor: Color = Color.Companion.Red.copy(alpha = 0.15f),
    lineSpacing: Dp = 15.dp,
    lineWidth: Dp = 5.dp
): Modifier = if (enabled) {
    this.then(
        Modifier.Companion
            .clipToBounds()
            .drawBehind {
                drawDiagonalLines(
                    color = lineColor,
                    spacing = lineSpacing,
                    strokeWidth = lineWidth,
                    density = this
                )
            }
    )
} else {
    this
}

private fun DrawScope.drawDiagonalLines(
    color: Color,
    spacing: Dp,
    strokeWidth: Dp,
    density: Density
) = with(density) {
    val spacePx = spacing.toPx()
    val strokePx = strokeWidth.toPx()

    var startX = -size.height
    while (startX < size.width) {
        drawLine(
            color = color,
            start = Offset(startX, size.height),
            end = Offset(startX + size.height, 0f),
            strokeWidth = strokePx
        )
        startX += spacePx
    }
}