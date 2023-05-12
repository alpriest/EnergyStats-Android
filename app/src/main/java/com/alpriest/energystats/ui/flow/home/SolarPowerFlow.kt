package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.alpriest.energystats.models.kWh
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Sunny
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SolarPowerFlow(amount: Double, todaysGeneration: Double, modifier: Modifier, iconHeight: Dp, themeStream: MutableStateFlow<AppTheme>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val glowing: Boolean
        val sunColor: Color
        var glowColor: Color = Color.Transparent
        val orange = Color(0xFFE29848)
        val theme by themeStream.collectAsState()

        when (amount.toInt()) {
            in 1 until 2 -> {
                glowing = false
                sunColor = Sunny
            }
            in 2 until 3 -> {
                glowing = true
                glowColor = Sunny
                sunColor = orange
            }
            in 3 until 500 -> {
                glowing = true
                glowColor = orange
                sunColor = Color.Red
            }
            else -> {
                glowing = false
                sunColor = MaterialTheme.colors.onBackground
                glowColor = Color.Transparent
            }
        }

        if (theme.showTotalYield) {
            val yieldString = todaysGeneration.kWh(theme.decimalPlaces)
            Text("Yield today $yieldString")
        }

        Box(
            modifier = Modifier
                .padding(4.dp)
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
                10f,
                0f,
                0f,
                glowColor
                    .copy(alpha = .5f)
                    .toArgb()
            )

            val center = with(LocalDensity.current) { Offset(x = iconHeight.toPx() / 2f, y = (iconHeight.toPx() / 2f)) }

            Canvas(
                modifier = Modifier
                    .requiredSize(width = iconHeight, height = iconHeight)
                    .fillMaxSize()
            ) {

                if (glowing) {
                    this.drawIntoCanvas {
                        it.drawCircle(
                            center = center,
                            radius = 28f,
                            paint = paint
                        )

                        for (degrees in 0 until 360 step 45) {
                            val offset = center.minus(Offset(x = 51f, y = 5f))
                            rotate(degrees = degrees.toFloat()) {
                                it.drawRoundRect(
                                    left = offset.x,
                                    top = offset.y,
                                    right = offset.x + 14f,
                                    bottom = offset.y + 8f,
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
                    radius = 28f,
                )

                for (degrees in 0 until 360 step 45) {
                    rotate(degrees = degrees.toFloat()) {
                        drawRoundRect(
                            color = sunColor,
                            size = Size(width = 14f, height = 8f),
                            topLeft = center.minus(Offset(x = 51f, y = 5f)),
                            cornerRadius = CornerRadius(x = 8f, y = 8f)
                        )
                    }
                }
            }
        }

        PowerFlowView(
            amount = amount,
            themeStream = themeStream,
            position = PowerFlowLinePosition.NONE
        )
    }
}

@Preview(showBackground = true, widthDp = 500)
@Composable
fun SolarPowerFlowViewPreview() {
    EnergyStatsTheme {
        Row(
            modifier = Modifier
                .height(300.dp)
                .wrapContentWidth()
        ) {
            SolarPowerFlow(
                amount = 0.0,
                todaysGeneration = 1.0,
                modifier = Modifier.width(100.dp),
                iconHeight = 40.dp,
                themeStream = MutableStateFlow(AppTheme.preview())
            )

            SolarPowerFlow(
                amount = 0.5,
                todaysGeneration = 1.0,
                modifier = Modifier.width(100.dp),
                iconHeight = 40.dp,
                themeStream = MutableStateFlow(AppTheme.preview())
            )

            SolarPowerFlow(
                amount = 1.5,
                todaysGeneration = 1.0,
                modifier = Modifier.width(100.dp),
                iconHeight = 40.dp,
                themeStream = MutableStateFlow(AppTheme.preview())
            )

            SolarPowerFlow(
                amount = 2.5,
                todaysGeneration = 1.0,
                modifier = Modifier.width(100.dp),
                iconHeight = 40.dp,
                themeStream = MutableStateFlow(AppTheme.preview())
            )

            SolarPowerFlow(
                amount = 3.5,
                todaysGeneration = 1.0,
                modifier = Modifier.width(100.dp),
                iconHeight = 40.dp,
                themeStream = MutableStateFlow(AppTheme.preview())
            )
        }
    }
}
