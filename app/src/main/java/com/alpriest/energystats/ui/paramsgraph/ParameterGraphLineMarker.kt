package com.alpriest.energystats.ui.paramsgraph

import android.annotation.SuppressLint
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ParameterGraphLineMarker(
    private val selectedValueStream: MutableStateFlow<ParameterGraphLineMarkerModel?>
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

        // Find first entry with points among line targets
        val lineEntry = lineTargets
            .asSequence()
            .mapNotNull { target -> target.points.firstOrNull()?.entry?.x }
            .firstOrNull()

        val entryX = lineEntry

        // If there are no usable entries at all, clear the selection and return
        if (entryX == null) {
            selectedValueStream.value = null
            return
        }

        val targetX = targets.averageOf { it.canvasX }

        selectedValueStream.value = ParameterGraphLineMarkerModel(
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
}

internal fun <T> Collection<T>.averageOf(selector: (T) -> Float): Float =
    fold(0f) { sum, element ->
        sum + selector(element)
    } / size
