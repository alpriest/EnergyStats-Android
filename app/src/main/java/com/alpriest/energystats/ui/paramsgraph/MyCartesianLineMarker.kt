package com.alpriest.energystats.ui.paramsgraph

import android.R.attr.label
import android.annotation.SuppressLint
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class MyCartesianLineMarker(
    private val selectedValueStream: MutableStateFlow<VerticalLineMarkerModel?>
) : CartesianMarker {
    override fun drawOverLayers(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ) {
        with(context) {
            drawLabel(context, targets)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun drawLabel(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ) {
        val lineTargets = targets.mapNotNull { it as? LineCartesianLayerMarkerTarget }
        val columnTargets = targets.mapNotNull { it as? ColumnCartesianLayerMarkerTarget }

        // Find first entry with points among line targets
        val lineEntry = lineTargets
            .asSequence()
            .mapNotNull { target -> target.points.firstOrNull()?.entry?.x }
            .firstOrNull()

        // If no suitable line entry, try column targets
        val columnEntry = if (lineEntry == null) {
            columnTargets
                .asSequence()
                .mapNotNull { target -> target.columns.firstOrNull()?.entry?.x }
                .firstOrNull()
        } else {
            null
        }

        val entryX = lineEntry ?: columnEntry

        // If there are no usable entries at all, clear the selection and return
        if (entryX == null) {
            selectedValueStream.value = null
            return
        }

        val targetX = targets.averageOf { it.canvasX }

        selectedValueStream.value = VerticalLineMarkerModel(
            context.layerBounds,
            targetX,
            entryX,
            LocalDateTime.ofInstant(
                Instant.ofEpochSecond(entryX.toLong()),
                ZoneId.systemDefault()
            )
        )
    }

    override fun updateLayerMargins(
        context: CartesianMeasuringContext,
        layerMargins: CartesianLayerMargins,
        layerDimensions: CartesianLayerDimensions,
        model: CartesianChartModel,
    ) {
    }

    override fun equals(other: Any?): Boolean =
        this === other

    override fun hashCode(): Int {
        return label.hashCode()
    }
}

internal fun <T> Collection<T>.averageOf(selector: (T) -> Float): Float =
    fold(0f) { sum, element ->
        sum + selector(element)
    } / size
