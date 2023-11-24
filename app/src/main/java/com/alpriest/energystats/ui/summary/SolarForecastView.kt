package com.alpriest.energystats.ui.summary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.BrushShader
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SolarForecastView(viewModel: SolarForecastViewModel) {
    LaunchedEffect(null) {
        viewModel.load()
    }

    val chartEntryModel = entryModelOf(
        entriesOf(4f, 12f, 8f, 16f),
        entriesOf(2f, 4f, 3f, 5f),
        entriesOf(3f, 7f, 5f, 8f)
    )
    val estimateColor = Color.Yellow.copy(alpha = 0.2f)

    Column(modifier = Modifier.fillMaxWidth()) {
        ProvideChartStyle {
            Chart(
                chart = lineChart(
                    lines = listOf(
                        LineChart.LineSpec(
                            lineColor = estimateColor.toArgb(),
                            lineBackgroundShader = BrushShader(
                                Brush.linearGradient(
                                    colors = listOf(estimateColor, estimateColor),
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, 0f)
                                )
                            )
                        ),
                        LineChart.LineSpec(
                            lineColor = estimateColor.toArgb(),
                            lineBackgroundShader = BrushShader(
                                Brush.linearGradient(
                                    colors = listOf(Color.White, Color.White),
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, 0f)
                                )
                            )
                        ),
                        LineChart.LineSpec(
                            lineColor = Color.Blue.toArgb(),
                        )
                    )
                ),
                model = chartEntryModel,
                chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                startAxis = rememberStartAxis(
                    itemPlacer = AxisItemPlacer.Vertical.default(5),
                    valueFormatter = DecimalFormatAxisValueFormatter("0.0")
                ),
                bottomAxis = rememberBottomAxis(
                    itemPlacer = AxisItemPlacer.Horizontal.default(2),
                    label = axisLabelComponent(horizontalPadding = 2.dp),
                    valueFormatter = SolarGraphFormatAxisValueFormatter(),
                    guideline = null
                ),
//                diffAnimationSpec = SnapSpec()
            )
        }
    }
}

class SolarGraphFormatAxisValueFormatter<Position : AxisPosition> : AxisValueFormatter<Position> {
    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
        return value.toInt().toString()
    }
}

@Preview(showBackground = true)
@Composable
fun SolarForecastViewPreview() {
    EnergyStatsTheme {
        SolarForecastView(
            viewModel = SolarForecastViewModel(
                solarForecastProvider = DemoSolarForecasting(),
                themeStream = MutableStateFlow(AppTheme.preview()),
            )
        )
    }
}
