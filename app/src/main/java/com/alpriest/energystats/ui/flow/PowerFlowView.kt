package com.alpriest.energystats.ui.flow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alpriest.energystats.shared.helpers.Wh
import com.alpriest.energystats.shared.helpers.isFlowing
import com.alpriest.energystats.shared.helpers.kW
import com.alpriest.energystats.shared.helpers.kWh
import com.alpriest.energystats.shared.helpers.w
import com.alpriest.energystats.shared.models.AppTheme
import com.alpriest.energystats.shared.models.DisplayUnit
import com.alpriest.energystats.shared.models.demo
import com.alpriest.energystats.shared.ui.PowerFlowNegative
import com.alpriest.energystats.shared.ui.PowerFlowNegativeText
import com.alpriest.energystats.shared.ui.PowerFlowNeutral
import com.alpriest.energystats.shared.ui.PowerFlowNeutralText
import com.alpriest.energystats.shared.ui.PowerFlowPositive
import com.alpriest.energystats.shared.ui.PowerFlowPositiveText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.abs

enum class PowerFlowLinePosition {
    LEFT,
    MIDDLE,
    RIGHT,
    NONE,
    HORIZONTAL
}

enum class LineOrientation {
    VERTICAL,
    HORIZONTAL
}

fun Double.power(displayUnit: DisplayUnit, decimalPlaces: Int): String {
    return when (displayUnit) {
        DisplayUnit.Watts -> this.w()
        DisplayUnit.Kilowatts -> this.kW(decimalPlaces)
        DisplayUnit.Adaptive -> return if (abs(this) < 1) {
            this.w()
        } else {
            this.kW(decimalPlaces)
        }
    }
}

fun Double.energy(displayUnit: DisplayUnit, decimalPlaces: Int): String {
    return when (displayUnit) {
        DisplayUnit.Watts -> this.Wh(decimalPlaces)
        DisplayUnit.Kilowatts -> this.kWh(decimalPlaces)
        DisplayUnit.Adaptive -> return if (abs(this) < 1) {
            this.Wh(decimalPlaces)
        } else {
            this.kWh(decimalPlaces)
        }
    }
}

@Composable
fun PowerText(amount: Double, themeStream: MutableStateFlow<AppTheme>, backgroundColor: Color, textColor: Color) {
    val theme by themeStream.collectAsStateWithLifecycle()
    val fontSize: TextUnit = theme.fontSize()

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Text(
            text = amount.power(theme.displayUnit, theme.decimalPlaces),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            modifier = Modifier
                .padding(vertical = 1.dp)
                .padding(horizontal = 3.dp)
        )
    }
}

@Composable
fun PowerFlowView(
    amount: Double,
    themeStream: MutableStateFlow<AppTheme>,
    position: PowerFlowLinePosition,
    modifier: Modifier = Modifier,
    useColouredLines: Boolean = false,
    orientation: LineOrientation
) {
    val theme by themeStream.collectAsStateWithLifecycle()
    val inverterColor = Color.LightGray
    val lineColor = if (amount.isFlowing() && useColouredLines && theme.useColouredLines) flowingColour(amount) else {
        PowerFlowNeutral
    }
    val strokeWidth = theme.strokeWidth()

    val powerTextColor = if (amount.isFlowing() && useColouredLines && theme.useColouredLines) textForeground(amount) else {
        PowerFlowNeutralText
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Line(amount, lineColor, Modifier, theme, orientation, amount.isFlowing())

        if (amount.isFlowing()) {
            PowerText(amount, themeStream, lineColor, powerTextColor)
        }

        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            when (position) {
                PowerFlowLinePosition.LEFT -> {
                    drawLine(
                        color = inverterColor,
                        start = Offset(size.width / 2 - (strokeWidth / 2), strokeWidth / 2),
                        end = Offset(size.width, strokeWidth / 2),
                        strokeWidth = strokeWidth
                    )
                }

                PowerFlowLinePosition.MIDDLE -> {
                    drawLine(
                        color = inverterColor,
                        start = Offset(0f, strokeWidth / 2),
                        end = Offset(size.width, strokeWidth / 2),
                        strokeWidth = strokeWidth
                    )
                }

                PowerFlowLinePosition.RIGHT -> {
                    drawLine(
                        color = inverterColor,
                        start = Offset(size.width / 2 + (strokeWidth / 2), strokeWidth / 2),
                        end = Offset(0f, strokeWidth / 2),
                        strokeWidth = strokeWidth
                    )
                }

                PowerFlowLinePosition.HORIZONTAL -> {
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, size.height * 0.7f),
                        end = Offset(size.width, size.height * 0.7f),
                        strokeWidth = strokeWidth
                    )
                }

                PowerFlowLinePosition.NONE -> {}
            }
        }
    }
}

fun flowingColour(amount: Double): Color {
    return if (amount < 0) {
        PowerFlowNegative
    } else {
        PowerFlowPositive
    }
}

fun textForeground(amount: Double): Color {
    return if (amount < 0) {
        PowerFlowNegativeText
    } else {
        PowerFlowPositiveText
    }
}

@Preview
@Composable
fun PowerFlowViewPreview() {
    Row(Modifier.height(200.dp)) {
        PowerFlowView(
            5.255,
            themeStream = MutableStateFlow(AppTheme.demo()),
            position = PowerFlowLinePosition.LEFT,
            orientation = LineOrientation.VERTICAL
        )
        PowerFlowView(
            5.255,
            themeStream = MutableStateFlow(AppTheme.demo(useLargeDisplay = true, showBatteryTemperature = true)),
            position = PowerFlowLinePosition.MIDDLE,
            orientation = LineOrientation.VERTICAL
        )
        PowerFlowView(
            -3.0,
            themeStream = MutableStateFlow(AppTheme.demo(useLargeDisplay = false, showBatteryTemperature = false)),
            position = PowerFlowLinePosition.RIGHT,
            orientation = LineOrientation.VERTICAL
        )
    }
}
