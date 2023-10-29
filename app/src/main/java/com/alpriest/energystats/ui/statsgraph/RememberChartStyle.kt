package com.alpriest.energystats.ui.statsgraph

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.theme.AppTheme
import com.patrykandpatrick.vico.compose.style.ChartStyle
import com.patrykandpatrick.vico.core.DefaultColors
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
internal fun chartStyle(chartColors: List<Color>, themeStream: MutableStateFlow<AppTheme>): ChartStyle {
    val isSystemInDarkTheme = isDarkMode(themeStream)
    val defaultColors = if (isSystemInDarkTheme) DefaultColors.Dark else DefaultColors.Light
    val axisGuidelineColor = if (isSystemInDarkTheme) Color.DarkGray else Color.LightGray.copy(alpha = 0.5f)

    return ChartStyle(
        ChartStyle.Axis(
            axisLabelColor = Color(defaultColors.axisLabelColor),
            axisGuidelineColor = axisGuidelineColor,
            axisLineColor = Color(defaultColors.axisLineColor),
            axisLabelTextAlign = Paint.Align.RIGHT,
            axisLabelTypeface = Typeface.DEFAULT,
            axisLabelTextSize = 8.sp
        ),
        ChartStyle.ColumnChart(
            chartColors.map { color ->
                LineComponent(
                    color.toArgb(),
                    DefaultDimens.COLUMN_WIDTH,
                    Shapes.rectShape
                )
            },
            outsideSpacing = 2.dp,
            innerSpacing = 2.dp
        ),
        ChartStyle.LineChart(
            chartColors.map { color ->
                LineChart.LineSpec(
                    lineColor = color.toArgb(),
                )
            },
        ),
        ChartStyle.Marker(),
        Color(defaultColors.elevationOverlayColor),
    )
}
