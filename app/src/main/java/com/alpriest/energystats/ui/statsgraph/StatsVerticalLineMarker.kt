package com.alpriest.energystats.ui.statsgraph

import android.content.Context
import android.graphics.RectF
import com.patrykandpatrick.vico.core.chart.composed.ComposedChart
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.marker.Marker
import kotlinx.coroutines.flow.MutableStateFlow

class StatsVerticalLineMarker(
    private var valuesAtTimeStream: MutableStateFlow<List<StatsChartEntry>>,
    private var graphVariablesStream: MutableStateFlow<List<StatsGraphVariable>>,
    private val composedChart: ComposedChart<ChartEntryModel>,
    private val guideline: LineComponent?,
    private val viewModel: StatsTabViewModel,
    private val context: Context
) : Marker {
    private val additionalBarWidth = 3.0f

    override fun draw(context: DrawContext, bounds: RectF, markedEntries: List<Marker.EntryModel>, chartValuesProvider: ChartValuesProvider) {
        val markedEntry = markedEntries.first()
        val graphVariables = graphVariablesStream.value

        val markedEntriesAtPosition = composedChart.charts[0].entryLocationMap.flatMap { modelList ->
            modelList.value.filter { it.index == markedEntry.index }
        }

        val chartEntries = markedEntriesAtPosition.mapNotNull { it.entry as? StatsChartEntry }

        valuesAtTimeStream.value = graphVariables.map { graphVariable ->
            chartEntries.firstOrNull { it.type == graphVariable.type } ?: StatsChartEntry(
                periodDescription = chartEntries.firstOrNull()?.periodDescription ?: "",
                x = chartEntries.firstOrNull()?.x ?: 0f,
                y = 0f,
                type = graphVariable.type
            )
        }
        viewModel.updateApproximationsFromSelectedValues(this.context)

        drawGuideline(context, bounds, markedEntriesAtPosition)
    }

    private fun drawGuideline(
        context: DrawContext,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>
    ) {
        if (markedEntries.isNotEmpty()) {
            val left = markedEntries.minOf { it.location.x } - additionalBarWidth
            val right = markedEntries.maxOf { it.location.x } + additionalBarWidth

            guideline?.draw(
                context,
                left,
                bounds.top,
                right,
                bounds.bottom
            )
        }
    }
}