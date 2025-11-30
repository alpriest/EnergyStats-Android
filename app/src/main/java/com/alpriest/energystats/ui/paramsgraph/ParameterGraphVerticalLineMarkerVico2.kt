package com.alpriest.energystats.ui.paramsgraph

import android.graphics.RectF
import com.patrykandpatrick.vico1.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico1.core.context.DrawContext
import com.patrykandpatrick.vico1.core.marker.Marker
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime

class ParameterGraphVerticalLineMarkerVico2(
    private var allProducers: Map<String, Pair<List<List<DateTimeFloatEntry>>, AxisScale>>,
    private var valuesAtTimeStream: MutableStateFlow<List<DateTimeFloatEntry>> = MutableStateFlow(listOf()),
    private var lastMarkerModel: MutableStateFlow<ParameterGraphVerticalLineMarkerModel?>
) : Marker {
    override fun draw(context: DrawContext, bounds: RectF, markedEntries: List<Marker.EntryModel>, chartValuesProvider: ChartValuesProvider) {
        markedEntries.firstOrNull()?.let { entryModel ->
            val allMarkedEntries: List<DateTimeFloatEntry> = allProducers.values
                .flatMap { (list, _) -> list.flatten() }
                .filter { it.x == entryModel.entry.x }

            val selectionTime = allMarkedEntries.firstNotNullOfOrNull { it }?.localDateTime

            lastMarkerModel.value = ParameterGraphVerticalLineMarkerModel(bounds, entryModel.location.x, selectionTime)

            valuesAtTimeStream.value = allMarkedEntries.map { markedEntry ->
                markedEntry.let {
                    DateTimeFloatEntry(it.localDateTime, it.x, it.y, it.type)
                }
            }
        }
    }
}

data class ParameterGraphVerticalLineMarkerModel(
    val bounds: RectF,
    val x: Float,
    val time: LocalDateTime?
)