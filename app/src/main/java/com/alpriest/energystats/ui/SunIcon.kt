package com.alpriest.energystats.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp

@Composable
fun SunIcon(
    size: Dp,
    color: Color,
    glowColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.requiredSize(width = size, height = size)
    ) {
        val paint = remember {
            Paint().apply {
                style = PaintingStyle.Stroke
                strokeWidth = 10f
            }
        }

        val frameworkPaint = remember {
            paint.asFrameworkPaint()
        }

        Canvas(
            modifier = Modifier
                .requiredSize(width = size, height = size)
                .fillMaxSize()
                .clipToBounds()
        ) {
            val center = Offset(x = size.toPx() / 2f, y = size.toPx() / 2f)
            val sunBarLength = size.value * 0.45f
            val sunBarWidth = size.value * 0.2f
            val sunBarOffsetX = size.value * 1f
            val centreCircleRadius = size.value * 0.4f

            val glow = glowColor ?: Color.Transparent

            if (glow != Color.Transparent) {
                val transparent = glow.copy(alpha = 0f).toArgb()
                frameworkPaint.color = transparent
                frameworkPaint.setShadowLayer(
                    15f,
                    0f,
                    0f,
                    glow.toArgb()
                )

                // Glow
                this.drawIntoCanvas {
                    it.drawCircle(
                        center = center,
                        radius = centreCircleRadius,
                        paint = paint
                    )

                    for (degrees in 0 until 360 step 45) {
                        val offset = center.minus(Offset(x = sunBarOffsetX, y = 5f))

                        rotate(degrees = degrees.toFloat()) {
                            it.drawRoundRect(
                                left = offset.x,
                                top = offset.y,
                                right = offset.x + sunBarLength,
                                bottom = offset.y + sunBarWidth,
                                paint = paint,
                                radiusX = 8f,
                                radiusY = 8f
                            )
                        }
                    }
                }
            }

            // Center circle
            drawCircle(
                color = color,
                center = center,
                radius = centreCircleRadius
            )

            // Sun rays
            for (degrees in 44 until 360 step 45) {
                rotate(degrees = degrees.toFloat()) {
                    drawRoundRect(
                        color = color,
                        size = Size(width = sunBarLength, height = sunBarWidth),
                        topLeft = center.minus(Offset(x = sunBarOffsetX, y = 5f)),
                        cornerRadius = CornerRadius(x = 4f, y = 4f)
                    )
                }
            }
        }
    }
}

