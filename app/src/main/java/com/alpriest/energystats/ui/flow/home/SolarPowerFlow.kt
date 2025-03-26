package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.flow.LineOrientation
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.flow.battery.iconBackgroundColor
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Sunny
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SolarPowerFlow(amount: Double, modifier: Modifier, iconHeight: Dp, themeStream: MutableStateFlow<AppTheme>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val glowing: Boolean
        val sunColor: Color
        var glowColor: Color = Color.Transparent
        val orange = Color(0xFFF2A53D)
        val theme by themeStream.collectAsState()

        if (amount >= 0.001f && amount < theme.solarRangeDefinitions.threshold1) {
            glowing = false
            sunColor = Sunny
        } else if (amount >= theme.solarRangeDefinitions.threshold1 && amount < theme.solarRangeDefinitions.threshold2) {
            glowing = true
            glowColor = Sunny.copy(alpha = 0.4f)
            sunColor = Sunny
        } else if (amount >= theme.solarRangeDefinitions.threshold2 && amount < theme.solarRangeDefinitions.threshold3) {
            glowing = true
            glowColor = Sunny.copy(alpha = 0.9f)
            sunColor = orange
        } else if (amount >= theme.solarRangeDefinitions.threshold3 && amount < 500f) {
            glowing = true
            glowColor = orange
            sunColor = Color.Red
        } else {
            glowing = false
            sunColor = iconBackgroundColor(isDarkMode(themeStream))
            glowColor = Color.Transparent
        }

        Box(
            modifier = Modifier
                .requiredSize(width = iconHeight, height = iconHeight)
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

            val transparent = glowColor
                .copy(alpha = 0f)
                .toArgb()

            frameworkPaint.color = transparent

            frameworkPaint.setShadowLayer(
                15f,
                0f,
                0f,
                glowColor
                    .toArgb()
            )

            val center = with(LocalDensity.current) { Offset(x = iconHeight.toPx() / 2f, y = (iconHeight.toPx() / 2f)) }

            Canvas(
                modifier = Modifier
                    .requiredSize(width = iconHeight, height = iconHeight)
                    .fillMaxSize()
                    .clipToBounds()
            ) {
                val sunBarLength = iconHeight.value * 0.45f
                val sunBarWidth = iconHeight.value * 0.2f
                val sunBarOffsetX = iconHeight.value * 1f
                val centreCircleRadius = iconHeight.value * 0.4f

                if (glowing) {
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

                drawCircle(
                    color = sunColor,
                    center = center,
                    radius = centreCircleRadius
                )

                for (degrees in 44 until 360 step 45) {
                    rotate(degrees = degrees.toFloat()) {
                        drawRoundRect(
                            color = sunColor,
                            size = Size(width = sunBarLength, height = sunBarWidth),
                            topLeft = center.minus(Offset(x = sunBarOffsetX, y = 5f)),
                            cornerRadius = CornerRadius(x = 4f, y = 4f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        PowerFlowView(
            amount = amount,
            themeStream = themeStream,
            position = PowerFlowLinePosition.NONE,
            orientation = LineOrientation.VERTICAL
        )
    }
}

@Preview(showBackground = true, widthDp = 500)
@Composable
fun SolarPowerFlowViewPreview() {
    val amount = remember { mutableFloatStateOf(0.0f) }

    EnergyStatsTheme {
        Column {
            SolarPowerFlow(
                amount = amount.value.toDouble(),
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp),
                iconHeight = 40.dp,
                themeStream = MutableStateFlow(AppTheme.demo().copy(showFinancialSummary = false))
            )

            Slider(
                value = amount.value,
                onValueChange = { amount.value = it },
                valueRange = 0.1f..5.0f
            )

            Row(
                modifier = Modifier
                    .height(300.dp)
                    .wrapContentWidth()
            ) {
                SolarPowerFlow(
                    amount = 0.0,
                    modifier = Modifier.width(100.dp),
                    iconHeight = 40.dp,
                    themeStream = MutableStateFlow(AppTheme.demo())
                )

                SolarPowerFlow(
                    amount = 0.5,
                    modifier = Modifier.width(100.dp),
                    iconHeight = 40.dp,
                    themeStream = MutableStateFlow(AppTheme.demo())
                )

                SolarPowerFlow(
                    amount = 1.5,
                    modifier = Modifier.width(100.dp),
                    iconHeight = 40.dp,
                    themeStream = MutableStateFlow(AppTheme.demo())
                )

                SolarPowerFlow(
                    amount = 2.5,
                    modifier = Modifier.width(100.dp),
                    iconHeight = 40.dp,
                    themeStream = MutableStateFlow(AppTheme.demo())
                )

                SolarPowerFlow(
                    amount = 3.5,
                    modifier = Modifier.width(100.dp),
                    iconHeight = 40.dp,
                    themeStream = MutableStateFlow(AppTheme.demo())
                )
            }
        }
    }
}
