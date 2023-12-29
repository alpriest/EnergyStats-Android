package com.alpriest.energystats.ui.paramsgraph

import android.graphics.RectF
import androidx.compose.animation.core.SnapSpec
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
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
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerVisibilityChangeListener
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ParameterGraphView(
    producer: ChartEntryModelProducer,
    viewModel: ParametersGraphTabViewModel,
    themeStream: MutableStateFlow<AppTheme>,
    modifier: Modifier = Modifier
) {
    val chartColors = viewModel.chartColorsStream.collectAsState().value
    val markerVisibilityChangeListener = object : MarkerVisibilityChangeListener {
        override fun onMarkerHidden(marker: Marker) {
            super.onMarkerHidden(marker)

            viewModel.valuesAtTimeStream.value = listOf()
        }
    }
    val entries = viewModel.entriesStream.collectAsState().value.firstOrNull() ?: listOf()

    val displayMode = viewModel.displayModeStream.collectAsState().value
    val formatter = ParameterGraphBottomAxisValueFormatter<AxisPosition.Horizontal.Bottom>()

    MonitorAlertDialog(viewModel)

    if (entries.isNotEmpty()) {
        when (displayMode.hours) {
            24 ->
                Column(modifier = modifier.fillMaxWidth()) {
                    ProvideChartStyle(chartStyle(chartColors, themeStream)) {
                        Chart(
                            chart = lineChart(
                                axisValuesOverrider = AxisValuesOverrider.fixed(0f, 288f)
                            ),
                            chartModelProducer = producer,
                            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                            endAxis = rememberEndAxis(
                                itemPlacer = AxisItemPlacer.Vertical.default(5),
                                valueFormatter = ParameterGraphEndAxisValueFormatter()
                            ),
                            bottomAxis = rememberBottomAxis(
                                itemPlacer = AxisItemPlacer.Horizontal.default(36, addExtremeLabelPadding = true),
                                valueFormatter = formatter,
                                tick = null,
                                guideline = axisGuidelineComponent()
                            ),
                            marker = NonDisplayingMarker(
                                viewModel.valuesAtTimeStream, lineComponent(
                                    color = colors.onSurface,
                                    thickness = 1.dp
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
                            chart = lineChart(),
                            chartModelProducer = producer,
                            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                            endAxis = rememberEndAxis(
                                itemPlacer = AxisItemPlacer.Vertical.default(5),
                                valueFormatter = ParameterGraphEndAxisValueFormatter()
                            ),
                            bottomAxis = rememberBottomAxis(
                                itemPlacer = AxisItemPlacer.Horizontal.default(9, addExtremeLabelPadding = true),
                                valueFormatter = ParameterGraphBottomAxisValueFormatter(),
                                tick = null,
                                guideline = axisGuidelineComponent()
                            ),
                            marker = NonDisplayingMarker(
                                viewModel.valuesAtTimeStream, lineComponent(
                                    color = colors.onSurface,
                                    thickness = 1.dp
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
                            chart = lineChart(),
                            chartModelProducer = producer,
                            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                            endAxis = rememberEndAxis(
                                itemPlacer = AxisItemPlacer.Vertical.default(5),
                                valueFormatter = ParameterGraphEndAxisValueFormatter()
                            ),
                            bottomAxis = rememberBottomAxis(
                                itemPlacer = AxisItemPlacer.Horizontal.default(spacing = 18, addExtremeLabelPadding = true),
                                valueFormatter = ParameterGraphBottomAxisValueFormatter(),
                                tick = null,
                                guideline = axisGuidelineComponent()
                            ),
                            marker = NonDisplayingMarker(
                                viewModel.valuesAtTimeStream, lineComponent(
                                    color = colors.onSurface,
                                    thickness = 1.dp
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
        return (chartValues.chartEntryModel.entries.first().firstOrNull { it.x == value } as? DateTimeFloatEntry)
            ?.localDateTime
            ?.run {
                String.format("%d:%02d", hour, minute)
            }
            .orEmpty()
    }
}

class ParameterGraphEndAxisValueFormatter<Position : AxisPosition> : AxisValueFormatter<Position> {
    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
        return (chartValues.chartEntryModel.entries.first().firstOrNull() as? DateTimeFloatEntry)
            ?.run {
                if (this.type.unit == "%") {
                    String.format("%d %s", value.toInt(), type.unit)
                } else {
                    String.format("%.02f %s", value, type.unit)
                }
            }
            .orEmpty()
    }
}

class NonDisplayingMarker<T>(
    private var valuesAtTimeStream: MutableStateFlow<List<T>> = MutableStateFlow(listOf()),
    private val guideline: LineComponent?
) : Marker {
    override fun draw(context: DrawContext, bounds: RectF, markedEntries: List<Marker.EntryModel>, chartValuesProvider: ChartValuesProvider) {
        drawGuideline(context, bounds, markedEntries)

        valuesAtTimeStream.value = markedEntries.mapNotNull { it.entry as? T }
    }

    private fun drawGuideline(
        context: DrawContext,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
    ) {
        markedEntries
            .map { it.location.x }
            .toSet()
            .forEach { x ->
                guideline?.drawVertical(
                    context,
                    bounds.top,
                    bounds.bottom,
                    x,
                )
            }
    }
}