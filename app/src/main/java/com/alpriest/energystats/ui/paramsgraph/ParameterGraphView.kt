package com.alpriest.energystats.ui.paramsgraph

import android.graphics.RectF
import android.widget.Toast
import androidx.compose.animation.core.SnapSpec
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.statsgraph.chartStyle
import com.alpriest.energystats.ui.theme.AppTheme
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.chart.scale.AutoScaleUp
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerVisibilityChangeListener
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ParameterGraphView(viewModel: ParametersGraphTabViewModel, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    val chartColors = viewModel.chartColorsStream.collectAsState().value
    val markerVisibilityChangeListener = object : MarkerVisibilityChangeListener {
        override fun onMarkerHidden(marker: Marker) {
            super.onMarkerHidden(marker)

            viewModel.valuesAtTimeStream.value = listOf()
        }
    }
    val entries = viewModel.entriesStream.collectAsState().value.firstOrNull() ?: listOf()
    val firstHour = entries.firstOrNull()?.localDateTime?.hour ?: 0
    val firstLabelOffset = entries.indexOfFirst { it.localDateTime.hour > firstHour }
    val context = LocalContext.current
    val toastMessage = viewModel.toastMessage.collectAsState().value

    toastMessage?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        viewModel.toastMessage.value = null
    }

    if (entries.isNotEmpty()) {
        Column(modifier = modifier.fillMaxWidth()) {
            ProvideChartStyle(chartStyle(chartColors, themeStream)) {
                Chart(
                    chart = lineChart(
                        axisValuesOverrider = AxisValuesOverrider.fixed(0f, 287f)
                    ),
                    chartModelProducer = viewModel.producer,
                    chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                    startAxis = rememberStartAxis(
                        itemPlacer = AxisItemPlacer.Vertical.default(5),
                        valueFormatter = DecimalFormatAxisValueFormatter("0.0")
                    ),
                    bottomAxis = rememberBottomAxis(
                        itemPlacer = AxisItemPlacer.Horizontal.default(24, firstLabelOffset),
                        valueFormatter = ParameterGraphFormatAxisValueFormatter(),
                        guideline = null
                    ),
                    marker = NonDisplayingMarker(
                        viewModel.valuesAtTimeStream, lineComponent(
                            color = colors.onSurface,
                            thickness = 1.dp
                        )
                    ),
                    diffAnimationSpec = SnapSpec(),
                    markerVisibilityChangeListener = markerVisibilityChangeListener
                )
            }
        }
    }
}

class ParameterGraphFormatAxisValueFormatter<Position : AxisPosition>() :
    AxisValueFormatter<Position> {

    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
        return (chartValues.chartEntryModel.entries.first().getOrNull(value.toInt()) as? DateTimeFloatEntry)
            ?.localDateTime
            ?.run {
                String.format("%d:%02d", hour, minute)
            }
            .orEmpty()
    }
}

class NonDisplayingMarker<T>(
    var valuesAtTimeStream: MutableStateFlow<List<T>> = MutableStateFlow(listOf()),
    val guideline: LineComponent?
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