package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.animation.core.SnapSpec
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.statsgraph.chartStyle
import com.alpriest.energystats.ui.theme.AppTheme
import com.patrykandpatrick.vico.compose.axis.axisGuidelineComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberEndAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.layout.fullWidth
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerVisibilityChangeListener
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
    modifier: Modifier = Modifier,
    showYAxisUnit: Boolean,
    userManager: UserManaging
) {
    val markerVisibilityChangeListener = object : MarkerVisibilityChangeListener {
        override fun onMarkerHidden(marker: Marker) {
            super.onMarkerHidden(marker)

            viewModel.valuesAtTimeStream.value = listOf()
        }
    }
    val entries = viewModel.entriesStream.collectAsState().value.firstOrNull() ?: listOf()
    val displayMode = viewModel.displayModeStream.collectAsState().value
    val formatter = ParameterGraphBottomAxisValueFormatter<AxisPosition.Horizontal.Bottom>()
    val bounds = viewModel.boundsStream.collectAsState().value

    val max = (bounds.maxByOrNull { it.max }?.max) ?: 0f
    val min = (bounds.minByOrNull { it.min }?.min) ?: 0f
    val range = max - min
    val endAxisFormatter = if (showYAxisUnit) ParameterGraphEndAxisValueFormatter<AxisPosition.Vertical.End>(range) else DecimalFormatAxisValueFormatter("0.0")
    val seriesCount = producer.getModel()?.entries?.count() ?: 0
    val truncatedYAxisOnParameterGraphs = themeStream.collectAsState().value.truncatedYAxisOnParameterGraphs

    MonitorAlertDialog(viewModel, userManager)

    if (entries.isNotEmpty()) {
        when (displayMode.hours) {
            24 ->
                Column(modifier = modifier.fillMaxWidth()) {
                    ProvideChartStyle(chartStyle(chartColors, themeStream)) {
                        Chart(
                            runInitialAnimation = truncatedYAxisOnParameterGraphs,
                            chart = lineChart(
                                axisValuesOverrider = AxisValuesOverrider.fixed(
                                    minX = 0.0f,
                                    maxX = max(288.0f, entries.count().toFloat()),
                                    minY = if (truncatedYAxisOnParameterGraphs) yAxisScale.min else null,
                                    maxY = if (truncatedYAxisOnParameterGraphs) yAxisScale.max else null
                                ),
                                targetVerticalAxisPosition = AxisPosition.Vertical.Start
                            ),
                            chartModelProducer = producer,
                            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                            endAxis = rememberEndAxis(
                                itemPlacer = AxisItemPlacer.Vertical.default(5),
                                valueFormatter = endAxisFormatter
                            ),
                            bottomAxis = rememberBottomAxis(
                                itemPlacer = AxisItemPlacer.Horizontal.default(24, addExtremeLabelPadding = true),
                                valueFormatter = formatter,
                                guideline = axisGuidelineComponent()
                            ),
                            marker = ParameterGraphVerticalLineMarker(
                                viewModel.valuesAtTimeStream,
                                lineComponent(
                                    color = colorScheme.onSurface,
                                    thickness = 1.dp
                                ),
                                textComponent(
                                    colorScheme.onSecondary,
                                    lineCount = seriesCount,
                                ),
                                shapeComponent(
                                    shape = Shapes.rectShape,
                                    color = colorScheme.secondary.copy(alpha = 0.3f),
                                    strokeColor = colorScheme.secondary,
                                    strokeWidth = 1.dp
                                )
                            ),
                            diffAnimationSpec = SnapSpec(),
                            markerVisibilityChangeListener = markerVisibilityChangeListener,
                            horizontalLayout = HorizontalLayout.fullWidth()
                        )
                    }
                }

            6 ->
                Column(modifier = modifier.fillMaxWidth()) {
                    ProvideChartStyle(chartStyle(chartColors, themeStream)) {
                        Chart(
                            runInitialAnimation = truncatedYAxisOnParameterGraphs,
                            chart = lineChart(
                                axisValuesOverrider = AxisValuesOverrider.fixed(
                                    minY = if (truncatedYAxisOnParameterGraphs) yAxisScale.min else null,
                                    maxY = if (truncatedYAxisOnParameterGraphs) yAxisScale.max else null
                                )
                            ),
                            chartModelProducer = producer,
                            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                            endAxis = rememberEndAxis(
                                itemPlacer = AxisItemPlacer.Vertical.default(5),
                                valueFormatter = endAxisFormatter
                            ),
                            bottomAxis = rememberBottomAxis(
                                itemPlacer = AxisItemPlacer.Horizontal.default(9, addExtremeLabelPadding = true),
                                valueFormatter = ParameterGraphBottomAxisValueFormatter(),
                                tick = null,
                                guideline = axisGuidelineComponent()
                            ),
                            marker = ParameterGraphVerticalLineMarker(
                                viewModel.valuesAtTimeStream,
                                lineComponent(
                                    color = colorScheme.onSurface,
                                    thickness = 1.dp
                                ),
                                textComponent(
                                    colorScheme.onSecondary,
                                    lineCount = seriesCount
                                ),
                                background = shapeComponent(
                                    shape = Shapes.rectShape,
                                    color = colorScheme.secondary.copy(alpha = 0.3f),
                                    strokeColor = colorScheme.secondary,
                                    strokeWidth = 1.dp
                                )
                            ),
                            diffAnimationSpec = SnapSpec(),
                            markerVisibilityChangeListener = markerVisibilityChangeListener,
                            horizontalLayout = HorizontalLayout.fullWidth()
                        )
                    }
                }

            else ->
                Column(modifier = modifier.fillMaxWidth()) {
                    ProvideChartStyle(chartStyle(chartColors, themeStream)) {
                        Chart(
                            runInitialAnimation = truncatedYAxisOnParameterGraphs,
                            chart = lineChart(
                                axisValuesOverrider = AxisValuesOverrider.fixed(
                                    minY = if (truncatedYAxisOnParameterGraphs) yAxisScale.min else null,
                                    maxY = if (truncatedYAxisOnParameterGraphs) yAxisScale.max else null
                                )
                            ),
                            chartModelProducer = producer,
                            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                            endAxis = rememberEndAxis(
                                itemPlacer = AxisItemPlacer.Vertical.default(5),
                                valueFormatter = endAxisFormatter
                            ),
                            bottomAxis = rememberBottomAxis(
                                itemPlacer = AxisItemPlacer.Horizontal.default(spacing = 18, addExtremeLabelPadding = true),
                                valueFormatter = ParameterGraphBottomAxisValueFormatter(),
                                tick = null,
                                guideline = axisGuidelineComponent()
                            ),
                            marker = ParameterGraphVerticalLineMarker(
                                viewModel.valuesAtTimeStream,
                                lineComponent(
                                    color = colorScheme.onSurface,
                                    thickness = 1.dp
                                ),
                                textComponent(
                                    colorScheme.onSecondary,
                                    lineCount = seriesCount,
                                    background = shapeComponent(
                                        shape = Shapes.rectShape,
                                        color = colorScheme.secondary,
                                    )
                                ),
                                shapeComponent(
                                    shape = Shapes.rectShape,
                                    color = colorScheme.secondary.copy(alpha = 0.3f),
                                    strokeColor = colorScheme.secondary,
                                    strokeWidth = 1.dp
                                )
                            ),
                            diffAnimationSpec = SnapSpec(),
                            markerVisibilityChangeListener = markerVisibilityChangeListener,
                            horizontalLayout = HorizontalLayout.fullWidth()
                        )
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
            ?.let { it as? DateTimeFloatEntry }
            ?.localDateTime?.toLocalTime()
            ?.run {
                String.format(Locale.getDefault(), "%02d", hour)
            }
            .orEmpty()
    }
}

class ParameterGraphEndAxisValueFormatter<Position : AxisPosition>(private val range: Float) : AxisValueFormatter<Position> {
    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
        return (chartValues.chartEntryModel.entries.first().firstOrNull() as? DateTimeFloatEntry)
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
