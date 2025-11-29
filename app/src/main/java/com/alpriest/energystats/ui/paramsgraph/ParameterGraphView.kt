package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.animation.core.SnapSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.statsgraph.chartStyle
import com.alpriest.energystats.ui.theme.AppTheme
import com.patrykandpatrick.vico1.compose.axis.axisGuidelineComponent
import com.patrykandpatrick.vico1.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico1.compose.axis.vertical.rememberEndAxis
import com.patrykandpatrick.vico1.compose.chart.Chart
import com.patrykandpatrick.vico1.compose.chart.layout.fullWidth
import com.patrykandpatrick.vico1.compose.chart.line.lineChart
import com.patrykandpatrick.vico1.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico1.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico1.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico1.core.axis.AxisPosition
import com.patrykandpatrick.vico1.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico1.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico1.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico1.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico1.core.chart.values.ChartValues
import com.patrykandpatrick.vico1.core.entry.ChartEntryModel
import com.patrykandpatrick.vico1.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

@Composable
fun ParameterGraphView(
    producer: ChartEntryModelProducer,
    yAxisScale: AxisScale,
    chartColors: List<Color>,
    viewModel: ParametersGraphTabViewModel,
    themeStream: MutableStateFlow<AppTheme>,
    showYAxisUnit: Boolean,
    userManager: UserManaging
) {
    val entries = viewModel.entriesStream.collectAsState().value.firstOrNull() ?: listOf()
    val displayMode = viewModel.displayModeStream.collectAsState().value
    val bounds = viewModel.boundsStream.collectAsState().value
    val producers = viewModel.producers.collectAsState().value

    val max = (bounds.maxByOrNull { it.max }?.max) ?: 0f
    val min = (bounds.minByOrNull { it.min }?.min) ?: 0f
    val range = max - min
    val endAxisFormatter = if (showYAxisUnit) ParameterGraphEndAxisValueFormatter<AxisPosition.Vertical.End>(range) else DecimalFormatAxisValueFormatter("0.0")
    val marker = ParameterGraphVerticalLineMarker(
        producers,
        viewModel.valuesAtTimeStream,
        viewModel.lastMarkerModelStream
    )
    val lastMarkerModel = viewModel.lastMarkerModelStream.collectAsState().value
    val truncatedYAxisOnParameterGraphs = themeStream.collectAsState().value.truncatedYAxisOnParameterGraphs

    MonitorAlertDialog(viewModel, userManager)

    if (entries.isNotEmpty()) {
        when (displayMode.hours) {
            24 -> ParameterGraphViewWithCustomMarker(
                producer,
                Modifier,
                chartColors,
                themeStream,
                endAxisFormatter,
                marker,
                lastMarkerModel,
                24,
                AxisValuesOverrider.fixed(
                    minX = 0.0f,
                    maxX = max(288.0f, entries.count().toFloat()),
                    minY = if (truncatedYAxisOnParameterGraphs) yAxisScale.min else null,
                    maxY = if (truncatedYAxisOnParameterGraphs) yAxisScale.max else null
                )
            )

            6 -> ParameterGraphViewWithCustomMarker(
                producer,
                Modifier,
                chartColors,
                themeStream,
                endAxisFormatter,
                marker,
                lastMarkerModel,
                9,
                AxisValuesOverrider.fixed(
                    minY = if (truncatedYAxisOnParameterGraphs) yAxisScale.min else null,
                    maxY = if (truncatedYAxisOnParameterGraphs) yAxisScale.max else null
                )
            )

            else -> ParameterGraphViewWithCustomMarker(
                producer,
                Modifier,
                chartColors,
                themeStream,
                endAxisFormatter,
                marker,
                lastMarkerModel,
                18,
                AxisValuesOverrider.fixed(
                    minY = if (truncatedYAxisOnParameterGraphs) yAxisScale.min else null,
                    maxY = if (truncatedYAxisOnParameterGraphs) yAxisScale.max else null
                )
            )
        }
    }
}

@Composable
fun ParameterGraphViewWithCustomMarker(
    producer: ChartEntryModelProducer,
    modifier: Modifier,
    chartColors: List<Color>,
    themeStream: MutableStateFlow<AppTheme>,
    endAxisFormatter: AxisValueFormatter<AxisPosition.Vertical.End>,
    marker: ParameterGraphVerticalLineMarker,
    lastMarkerModel: ParameterGraphVerticalLineMarkerModel?,
    horizontalAxisSpacing: Int,
    axisValuesOverrider: AxisValuesOverrider<ChartEntryModel>
) {
    val truncatedYAxisOnParameterGraphs = themeStream.collectAsState().value.truncatedYAxisOnParameterGraphs
    val formatter = remember { ParameterGraphBottomAxisValueFormatter<AxisPosition.Horizontal.Bottom>() }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            ProvideChartStyle(chartStyle(chartColors, themeStream)) {
                Chart(
                    runInitialAnimation = truncatedYAxisOnParameterGraphs,
                    chart = lineChart(
                        axisValuesOverrider = axisValuesOverrider,
                        targetVerticalAxisPosition = AxisPosition.Vertical.Start
                    ),
                    chartModelProducer = producer,
                    chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                    endAxis = rememberEndAxis(
                        itemPlacer = AxisItemPlacer.Vertical.default(5),
                        valueFormatter = endAxisFormatter
                    ),
                    bottomAxis = rememberBottomAxis(
                        itemPlacer = AxisItemPlacer.Horizontal.default(horizontalAxisSpacing, addExtremeLabelPadding = true),
                        valueFormatter = formatter,
                        guideline = axisGuidelineComponent()
                    ),
                    marker = marker,
                    diffAnimationSpec = SnapSpec(),
                    horizontalLayout = HorizontalLayout.fullWidth()
                )
            }

            lastMarkerModel?.let {
                producer.getModel()?.let { model ->
                    SelectedParameterValuesLineMarkerVico1(model.entries, it, themeStream)
                }
            }
        }
    }
}

class ParameterGraphBottomAxisValueFormatter<Position : AxisPosition> : AxisValueFormatter<Position> {
    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
        return chartValues.chartEntryModel.entries
            .asSequence()
            .flatMap { it.asSequence() }
            .firstOrNull { it.x == value }
            ?.let { it as? DateTimeFloatEntryVico1 }
            ?.localDateTime?.toLocalTime()
            ?.run {
                String.format(Locale.getDefault(), "%02d", hour)
            }
            .orEmpty()
    }
}

class ParameterGraphEndAxisValueFormatter<Position : AxisPosition>(private val range: Float) : AxisValueFormatter<Position> {
    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
        return (chartValues.chartEntryModel.entries.first().firstOrNull() as? DateTimeFloatEntryVico1)
            ?.run {
                if (this.type.unit == "%") {
                    String.format(Locale.getDefault(), "%d %s", value.toInt(), type.unit)
                } else {
                    if (abs(range.toDouble()) < 1.0) {
                        String.format(Locale.getDefault(), "%.2f %s", value, type.unit)
                    } else {
                        String.format(Locale.getDefault(), "%.1f %s", value, type.unit)
                    }
                }
            }
            .orEmpty()
    }
}
