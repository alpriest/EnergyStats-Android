package com.alpriest.energystats.ui.statsgraph

import android.content.Context
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.paramsgraph.LineMarkerColor
import com.alpriest.energystats.ui.theme.AppTheme
import com.patrykandpatrick.vico.core.chart.composed.ComposedChart
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.model.Point
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SelectedStatsValuesLineMarker(
    model: StatsGraphVerticalLineMarkerModel,
    themeStream: MutableStateFlow<AppTheme>
) {
    val additionalBarWidth = 3.0f

    if (model.markedEntries.isEmpty()) {
        return
    }

    val left = model.markedEntries.minOf { it.location.x } - additionalBarWidth
    val right = model.markedEntries.maxOf { it.location.x } + additionalBarWidth
    val color = LineMarkerColor(isDarkMode(themeStream))

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color.copy(alpha = 0.4f),
            topLeft = Offset(left, 24f),
            size = Size(width = right - left, model.bounds.height() + 4f)
        )
    }
}

data class StatsGraphVerticalLineMarkerModel(
    val context: DrawContext,
    val bounds: RectF,
    val location: Point,
    val markedEntries: List<Marker.EntryModel>
)

class StatsVerticalLineMarker(
    private var valuesAtTimeStream: MutableStateFlow<List<StatsChartEntry>>,
    private var graphVariablesStream: MutableStateFlow<List<StatsGraphVariable>>,
    private val composedChart: ComposedChart<ChartEntryModel>,
    private val viewModel: StatsTabViewModel,
    private val context: Context,
    private var lastMarkerModelStream: MutableStateFlow<StatsGraphVerticalLineMarkerModel?>
) : Marker {
    override fun draw(context: DrawContext, bounds: RectF, markedEntries: List<Marker.EntryModel>, chartValuesProvider: ChartValuesProvider) {
        val markedEntry = markedEntries.first()
        val graphVariables = graphVariablesStream.value

        val chartMarkedEntriesAtPosition = composedChart.charts.flatMap {
            it.entryLocationMap.flatMap { modelList ->
                modelList.value.filter { it.index == markedEntry.index }
            }
        }

        val chartEntries = chartMarkedEntriesAtPosition.mapNotNull { it.entry as? StatsChartEntry }

        valuesAtTimeStream.value = graphVariables.map { graphVariable ->
            chartEntries.firstOrNull { it.type == graphVariable.type } ?: StatsChartEntry(
                periodDescription = chartEntries.firstOrNull()?.periodDescription ?: "",
                x = chartEntries.firstOrNull()?.x ?: 0f,
                y = 0f,
                type = graphVariable.type
            )
        }
        viewModel.updateApproximationsFromSelectedValues(this.context)

        lastMarkerModelStream.value = StatsGraphVerticalLineMarkerModel(context, bounds, markedEntry.location, chartMarkedEntriesAtPosition)
    }
}
