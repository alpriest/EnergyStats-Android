package com.alpriest.energystats.ui.statsgraph

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.statsgraph.StatsDisplayMode.Day
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.IconColorInDarkTheme
import com.alpriest.energystats.ui.theme.IconColorInLightTheme
import com.alpriest.energystats.ui.theme.demo
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberEndAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.layout.fullWidth
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.chart.composed.plus
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.composed.plus
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun StatsGraphView(viewModel: StatsTabViewModel, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    val displayMode = viewModel.displayModeStream.collectAsState().value
    val chartColors = viewModel.chartColorsStream.collectAsState().value.map { it.colour(themeStream) }
    val selfSufficiencyGraphData = viewModel.selfSufficiencyGraphDataStream.collectAsState().value
    val statsGraphData = viewModel.statsGraphDataStream.collectAsState().value

    if (statsGraphData == null) {
        Text("No data")
    } else {
        val columnChart = columnChart(
            columns = chartColors.map { lineComponent(color = it) }.toList(),
            axisValuesOverrider = ZeroValuesAxisOverrider(),
            targetVerticalAxisPosition = AxisPosition.Vertical.End
        )
        val lineChart = lineChart(
            lines = listOf(
                lineSpec(
                    lineColor = selfSufficiencyLineColor(isDarkMode(themeStream)),
                    lineThickness = 1.dp,
                    lineBackgroundShader = null,
                )
            ),
            targetVerticalAxisPosition = AxisPosition.Vertical.Start
        )
        val composedChart = remember(columnChart, lineChart) { columnChart + lineChart }

        if (selfSufficiencyGraphData != null) {
            Column(modifier = modifier.fillMaxWidth()) {
                ProvideChartStyle(chartStyle(chartColors, themeStream)) {
                    Chart(
                        chart = composedChart,
                        model = statsGraphData + selfSufficiencyGraphData,
                        chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                        endAxis = rememberEndAxis(
                            itemPlacer = AxisItemPlacer.Vertical.default(5),
                            valueFormatter = DecimalFormatAxisValueFormatter("0.0")
                        ),
                        bottomAxis = rememberBottomAxis(
                            itemPlacer = AxisItemPlacer.Horizontal.default(3, addExtremeLabelPadding = true),
                            valueFormatter = StatsGraphFormatAxisValueFormatter(displayMode),
                            guideline = null
                        ),
                        horizontalLayout = HorizontalLayout.fullWidth()
                    )
                }
                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(
                        when (displayMode) {
                            is Day -> stringResource(R.string.hours)
                            is StatsDisplayMode.Month -> stringResource(R.string.days)
                            is StatsDisplayMode.Year -> stringResource(R.string.months)
                            is StatsDisplayMode.Custom -> stringResource(R.string.days)
                        }
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun StatsGraphViewPreview() {
    StatsGraphView(
        StatsTabViewModel(FakeConfigManager(), DemoNetworking()) { _, _ -> null },
        MutableStateFlow(AppTheme.demo())
    )
}

class StatsGraphFormatAxisValueFormatter<Position : AxisPosition>(private val displayMode: StatsDisplayMode) :
    AxisValueFormatter<Position> {

    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
        return when (displayMode) {
            is Day -> String.format(Locale.getDefault(), "%d:00", value.toInt())
            is StatsDisplayMode.Month -> value.toInt().toString()
            is StatsDisplayMode.Year -> {
                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.MONTH, value.toInt() - 1)
                return monthFormat.format(calendar.time)
            }

            is StatsDisplayMode.Custom -> displayMode.start.plusDays(value.toLong()).dayOfMonth.toString()
        }
    }
}

class ZeroValuesAxisOverrider : AxisValuesOverrider<ChartEntryModel> {
    override fun getMaxY(model: ChartEntryModel) = if (model.maxY != 0f) model.maxY else 1f
}

@Composable
fun selfSufficiencyLineColor(isDarkMode: Boolean): Color {
    return if (isDarkMode) {
        IconColorInDarkTheme
    } else {
        IconColorInLightTheme
    }
}
