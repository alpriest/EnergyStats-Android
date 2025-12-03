package com.alpriest.energystats.ui.paramsgraph

import android.graphics.Color
import android.graphics.RectF
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.alpriest.energystats.models.Variable
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico1.core.extension.averageOf
import java.text.DecimalFormat

open class MyCartesianMarker(
    protected val label: TextComponent,
    protected val valueFormatter: ValueFormatter = ValueFormatter.default(),
    protected val guideline: LineComponent? = null,
) : CartesianMarker {

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
            val label = TextComponent(
                lineCount = 4,
                padding = Insets(horizontalDp = 4.0f, verticalDp = 2.0f),
                background = ShapeComponent(
                    Fill(Color.WHITE),
                    strokeFill = Fill(Color.BLACK),
                    strokeThicknessDp = 1.0f
                )
            )
            val text = valueFormatter.format(context, targets)
            val targetX = targets.averageOf { it.canvasX }
            val labelBounds = label.getBounds(context, text, layerBounds.width().toInt())
            val textWidth = labelBounds.width()
            val x = overrideXPositionToFit(targetX, layerBounds, textWidth)

            val y: Float = context.layerBounds.top

            label.draw(
                context = context,
                text = text,
                x = x,
                y = y,
                horizontalPosition = Position.Horizontal.Start,
                verticalPosition = Position.Vertical.Bottom,
            )
        }

    protected fun overrideXPositionToFit(
        xPosition: Float,
        bounds: RectF,
        textWidth: Float,
    ): Float =
        when {
            xPosition - textWidth < bounds.left -> xPosition + textWidth
            xPosition + textWidth > bounds.right -> xPosition
            else -> xPosition + textWidth
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
        fun format(
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
            ): ValueFormatter = MyCartesianMarkerValueFormatter(decimalFormat, colorCode)
        }
    }
}

internal class MyCartesianMarkerValueFormatter(
    private val decimalFormat: DecimalFormat,
    private val colorCode: Boolean,
) : MyCartesianMarker.ValueFormatter {
    private fun SpannableStringBuilder.append(target: CartesianMarker.Target, variable: Variable?) {
        val lineTarget = target as? LineCartesianLayerMarkerTarget ?: return

        lineTarget.points.forEachIndexed { index, point ->
            append(variable?.name)
            append(decimalFormat.format(point.entry.y))
            append(variable?.unit)
            if (index != target.points.lastIndex) append("\n")
        }
    }

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ): CharSequence =
        SpannableStringBuilder().apply {
            targets.forEachIndexed { index, target ->
                val variable = context.model.models[index].extraStore.getOrNull(VariableKey)
                append(target, variable)
                if (index != targets.lastIndex) append(", ")
            }
        }

    override fun equals(other: Any?): Boolean =
        this === other ||
                other is MyCartesianMarkerValueFormatter &&
                decimalFormat == other.decimalFormat &&
                colorCode == other.colorCode

    override fun hashCode(): Int = 31 * decimalFormat.hashCode() + colorCode.hashCode()

    private data class ColorSpan(private val color: Int) : ForegroundColorSpan(color)
}

private inline val Float.half: Float
    get() = this / 2
