package com.alpriest.energystats.ui.paramsgraph

import android.graphics.RectF
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.orZero
import com.patrykandpatrick.vico.core.common.shape.MarkerCorneredShape
import com.patrykandpatrick.vico1.core.extension.appendCompat
import com.patrykandpatrick.vico1.core.extension.averageOf
import com.patrykandpatrick.vico1.core.extension.doubled
import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.math.min

open class MyCartesianMarker(
    protected val label: TextComponent,
    protected val valueFormatter: ValueFormatter = ValueFormatter.default(),
    protected val guideline: LineComponent? = null,
) : CartesianMarker {

    protected val markerCorneredShape: MarkerCorneredShape? =
        (label.background as? ShapeComponent)?.shape as? MarkerCorneredShape

    protected val tickSizeDp: Float = markerCorneredShape?.tickSizeDp.orZero

    override fun drawOverLayers(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ) {
        with(context) {
            drawGuideline(targets)
            drawLabel(context, targets)
        }
    }

    protected fun drawLabel(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ): Unit =
        with(context) {
            val text = valueFormatter.format(context, targets)
            val targetX = targets.averageOf { it.canvasX }
            val labelBounds = label.getBounds(context, text, layerBounds.width().toInt())
            val halfOfTextWidth = labelBounds.width().half
            val x = overrideXPositionToFit(targetX, layerBounds, halfOfTextWidth)
            markerCorneredShape?.tickX = targetX
            val tickPosition: MarkerCorneredShape.TickPosition
            val y: Float
            val verticalPosition: Position.Vertical

            tickPosition = MarkerCorneredShape.TickPosition.Bottom
            y = context.layerBounds.top - tickSizeDp.pixels
            verticalPosition = Position.Vertical.Top

            markerCorneredShape?.tickPosition = tickPosition

            label.draw(
                context = context,
                text = text,
                x = x,
                y = y,
                verticalPosition = verticalPosition,
                maxWidth = ceil(min(layerBounds.right - x, x - layerBounds.left).doubled).toInt(),
            )
        }

    protected fun overrideXPositionToFit(
        xPosition: Float,
        bounds: RectF,
        halfOfTextWidth: Float,
    ): Float =
        when {
            xPosition - halfOfTextWidth < bounds.left -> bounds.left + halfOfTextWidth
            xPosition + halfOfTextWidth > bounds.right -> bounds.right - halfOfTextWidth
            else -> xPosition
        }

    protected fun CartesianDrawingContext.drawGuideline(targets: List<CartesianMarker.Target>) {
        targets
            .map { it.canvasX }
            .toSet()
            .forEach { x -> guideline?.drawVertical(this, x, layerBounds.top, layerBounds.bottom) }
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
        var result = label.hashCode()
        result = 31 * result + valueFormatter.hashCode()
        result = 31 * result + guideline.hashCode()
        return result
    }

    /** Formats [CartesianMarker] values for display. */
    fun interface ValueFormatter {
        /** Returns a label for the given [CartesianMarker.Target]s. */
        public fun format(
            context: CartesianDrawingContext,
            targets: List<CartesianMarker.Target>,
        ): CharSequence

        /** Houses a [ValueFormatter] factory function. */
        companion object {
            /**
             * Creates an instance of the default [ValueFormatter] implementation. The labels produced
             * include the [CartesianLayerModel.Entry] _y_ values, which are formatted via [decimalFormat]
             * and, if [colorCode] is true, color-coded.
             */
            fun default(
                decimalFormat: DecimalFormat = DecimalFormat("#.##;−#.##"),
                colorCode: Boolean = true,
            ): ValueFormatter = DefaultValueFormatter(decimalFormat, colorCode)
        }
    }
}

internal class DefaultValueFormatter(
    private val decimalFormat: DecimalFormat,
    private val colorCode: Boolean,
) : MyCartesianMarker.ValueFormatter {
    private fun SpannableStringBuilder.append(y: Double, color: Int? = null) {
        if (colorCode && color != null) {
            appendCompat(decimalFormat.format(y), ColorSpan(color), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            append(decimalFormat.format(y))
        }
    }

    private fun SpannableStringBuilder.append(target: CartesianMarker.Target, unit: String?) {
        val lineTarget = target as? LineCartesianLayerMarkerTarget ?: return

        lineTarget.points.forEachIndexed { index, point ->
            append(point.entry.y, point.color)
            append(unit)
            if (index != target.points.lastIndex) append(", ")
        }
    }

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ): CharSequence =
        SpannableStringBuilder().apply {
            targets.forEachIndexed { index, target ->
                val unit = context.model.models[index].extraStore.getOrNull(UnitKey)
                append(target, unit)
                if (index != targets.lastIndex) append(", ")
            }
        }

    override fun equals(other: Any?): Boolean =
        this === other ||
                other is DefaultValueFormatter &&
                decimalFormat == other.decimalFormat &&
                colorCode == other.colorCode

    override fun hashCode(): Int = 31 * decimalFormat.hashCode() + colorCode.hashCode()

    private data class ColorSpan(private val color: Int) : ForegroundColorSpan(color)
}

private inline val Float.half: Float
    get() = this / 2
