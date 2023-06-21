package com.alpriest.energystats.ui.paramsgraph

import android.graphics.RectF
import androidx.compose.animation.core.SnapSpec
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.statsgraph.chartStyle
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.marker.Marker
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ParameterGraphView(viewModel: ParametersGraphTabViewModel, modifier: Modifier = Modifier) {
    val chartColors = viewModel.chartColorsStream.collectAsState().value
    val maxY = viewModel.maxYStream.collectAsState().value

    Column(modifier = modifier.fillMaxWidth()) {
        ProvideChartStyle(chartStyle(chartColors)) {
            Chart(
                chart = lineChart(
                    axisValuesOverrider = AxisValuesOverrider.fixed(minY = 0f, maxY = maxY)
                ),
                chartModelProducer = viewModel.producer,
                chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                startAxis = startAxis(
                    maxLabelCount = 5,
                    valueFormatter = DecimalFormatAxisValueFormatter("0.0")
                ),
                bottomAxis = bottomAxis(
                    label = axisLabelComponent(horizontalPadding = 2.dp),
                    tickPosition = HorizontalAxis.TickPosition.Center(offset = 0, spacing = 20),
                    valueFormatter = ParameterGraphFormatAxisValueFormatter()
                ),
                marker = NonDisplayingMarker(viewModel.valuesAtTimeStream, lineComponent(
                    color = MaterialTheme.colorScheme.onSurface,
                    thickness = 2.dp
                )),
                diffAnimationSpec = SnapSpec()
            )
        }
    }
}

class ParameterGraphFormatAxisValueFormatter<Position : AxisPosition>() :
    AxisValueFormatter<Position> {

    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
        return (chartValues.chartEntryModel.entries.first().getOrNull(value.toInt()) as? DateTimeFloatEntry)
            ?.localDateTime
            ?.run {
                "$hour"
            }
            .orEmpty()
    }
}

class NonDisplayingMarker(
    var valuesAtTimeStream: MutableStateFlow<List<DateTimeFloatEntry>> = MutableStateFlow(listOf()),
    val guideline: LineComponent?
) : Marker {
    override fun draw(context: DrawContext, bounds: RectF, markedEntries: List<Marker.EntryModel>) {
        drawGuideline(context, bounds, markedEntries)

        valuesAtTimeStream.value = markedEntries.mapNotNull { it.entry as? DateTimeFloatEntry }
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