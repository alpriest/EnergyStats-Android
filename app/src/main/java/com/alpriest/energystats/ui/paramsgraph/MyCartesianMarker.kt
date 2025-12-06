package com.alpriest.energystats.ui.paramsgraph

import android.R.attr.label
import android.annotation.SuppressLint
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico1.core.extension.averageOf
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class MyCartesianMarker(
    private val selectedValueStream: MutableStateFlow<ParameterGraphVerticalLineMarkerModel?>
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
        val targetX = targets.averageOf { it.canvasX }

        selectedValueStream.value = ParameterGraphVerticalLineMarkerModel(
            context.layerBounds,
            targetX,
            LocalDateTime.ofInstant(
                Instant.ofEpochSecond(lineTargets.first().points.first().entry.x.toLong()),
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