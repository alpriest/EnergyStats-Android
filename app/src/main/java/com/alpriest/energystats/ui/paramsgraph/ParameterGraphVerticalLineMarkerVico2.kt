import android.graphics.RectF
import java.time.LocalDateTime

//package com.alpriest.energystats.ui.paramsgraph
//
//import android.graphics.RectF
//import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
//import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
//import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
//import kotlinx.coroutines.flow.MutableStateFlow
//import java.time.LocalDateTime
//
//class ParameterGraphVerticalLineMarkerVico2(
//    private var allProducers: Map<String, Pair<List<List<DateTimeFloatEntry>>, AxisScale>>,
//    private val valuesAtTimeStream: MutableStateFlow<List<DateTimeFloatEntry>> = MutableStateFlow(emptyList()),
//    private val lastMarkerModel: MutableStateFlow<ParameterGraphVerticalLineMarkerModel?>
//) : CartesianMarker {
//    override fun drawOverLayers(context: CartesianDrawingContext, targets: List<CartesianMarker.Target>) {
//        super.drawOverLayers(context, targets)
//
//        val firstTarget = targets.firstOrNull() as? LineCartesianLayerMarkerTarget ?: return
//
//        firstTarget.points.forEachIndexed { index, point ->
//            val unit = context.model.models[index].extraStore.getOrNull(VariableKey)
//        }
//
//        // Find all entries across all producers with the same x.
//        val allEntriesAtMarker: List<DateTimeFloatEntry> = allProducers.values
//            .flatMap { (seriesLists, _) -> seriesLists.flatten() }
////            .filter {
////                it.graphPoint == targetX
////            }
//
//        val selectionTime: LocalDateTime? = allEntriesAtMarker.firstOrNull()?.localDateTime
//
//        // Save last marker position and time using the provided bounds.
//        lastMarkerModel.value = ParameterGraphVerticalLineMarkerModel(
//            bounds = context.layerBounds,
//            x = firstTarget.canvasX,
//            time = selectionTime
//        )
//
//        // Push all values at this x into the stream.
//        valuesAtTimeStream.value = allEntriesAtMarker.map { markedEntry ->
//            DateTimeFloatEntry(
//                localDateTime = markedEntry.localDateTime,
//                x = markedEntry.x,
//                y = markedEntry.y,
//                type = markedEntry.type
//            )
//        }
//    }
//}
//
data class ParameterGraphVerticalLineMarkerModel(
    val bounds: RectF,
    val x: Float,
    val time: LocalDateTime?
)