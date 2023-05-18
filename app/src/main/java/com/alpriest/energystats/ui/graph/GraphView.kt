package com.alpriest.energystats.ui.graph

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.style.ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.DefaultAlpha
import com.patrykandpatrick.vico.core.DefaultColors
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import kotlinx.coroutines.launch

class GraphViewModel(
    configManager: ConfigManaging,
    networking: Networking
) : ViewModel() {
    var chartColors = listOf<Color>()
    val producer: ChartEntryModelProducer = ChartEntryModelProducer()

    init {
        viewModelScope.launch {
//            configManager.currentDevice.value?.let {
            val variables = listOf(
                ReportVariable.Generation,
                ReportVariable.FeedIn,
                ReportVariable.GridConsumption,
                ReportVariable.ChargeEnergyToTal,
                ReportVariable.DischargeEnergyToTal
            )

            chartColors = variables.map { it.colour() }

            val reportData = networking.fetchReport(
                "123",
                variables = variables.toTypedArray(),
                queryDate = QueryDate(2023, 5, 14)
            )

            val entries = reportData
                .groupBy { it.variable }
                .map { group ->
                    group.value.flatMap {
                        it.data.map {
                            FloatEntry(x = it.index.toFloat(), y = it.value.toFloat())
                        }
                    }.toList()
                }.toList()

            chartColors = reportData
                .groupBy { it.variable }
                .map { ReportVariable.parse(it.value.first().variable).colour() }

            producer.setEntries(entries)
        }
//        }
    }
}

private fun ReportVariable.Companion.parse(variable: String): ReportVariable {
    return when (variable.lowercase()) {
        "feedin" -> ReportVariable.FeedIn
        "generation" -> ReportVariable.Generation
        "gridconsumption" -> ReportVariable.GridConsumption
        "chargeenergytotal" -> ReportVariable.ChargeEnergyToTal
        "dischargeenergytotal" -> ReportVariable.DischargeEnergyToTal
        else -> {
            ReportVariable.FeedIn
        }
    }
}

@Composable
internal fun rememberChartStyle(columnChartColors: List<Color>, lineChartColors: List<Color>): ChartStyle {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    return remember(columnChartColors, lineChartColors, isSystemInDarkTheme) {
        val defaultColors = if (isSystemInDarkTheme) DefaultColors.Dark else DefaultColors.Light
        ChartStyle(
            ChartStyle.Axis(
                axisLabelColor = Color(defaultColors.axisLabelColor),
                axisGuidelineColor = Color.Transparent,
                axisLineColor = Color(defaultColors.axisLineColor),
                axisLabelTextAlign = android.graphics.Paint.Align.RIGHT
            ),
            ChartStyle.ColumnChart(
                columnChartColors.map { columnChartColor ->
                    LineComponent(
                        columnChartColor.toArgb(),
                        DefaultDimens.COLUMN_WIDTH,
                        Shapes.rectShape,
                    )
                },
            ),
            ChartStyle.LineChart(
                lineChartColors.map { lineChartColor ->
                    LineChart.LineSpec(
                        lineColor = lineChartColor.toArgb(),
                        lineBackgroundShader = DynamicShaders.fromBrush(
                            Brush.verticalGradient(
                                listOf(
                                    lineChartColor.copy(DefaultAlpha.LINE_BACKGROUND_SHADER_START),
                                    lineChartColor.copy(DefaultAlpha.LINE_BACKGROUND_SHADER_END),
                                ),
                            ),
                        ),
                    )
                },
            ),
            ChartStyle.Marker(),
            Color(defaultColors.elevationOverlayColor),
        )
    }
}

@Composable
internal fun rememberChartStyle(chartColors: List<Color>) =
    rememberChartStyle(columnChartColors = chartColors, lineChartColors = chartColors)

@Composable
fun GraphView(viewModel: GraphViewModel) {
    ProvideChartStyle(rememberChartStyle(viewModel.chartColors)) {
        Chart(
            chart = columnChart(),
            chartModelProducer = viewModel.producer,
            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
            startAxis = startAxis(
                maxLabelCount = 5,
                valueFormatter = DecimalFormatAxisValueFormatter("0.0")
            ),
            bottomAxis = bottomAxis(
                tickPosition = HorizontalAxis.TickPosition.Center(spacing = 2)
            ),
        )
    }
}

@Composable
@Preview(showBackground = true)
fun GraphViewPreview() {
    GraphView(GraphViewModel(FakeConfigManager(), DemoNetworking()))
}