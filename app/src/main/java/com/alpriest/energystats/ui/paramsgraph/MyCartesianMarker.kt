package com.alpriest.energystats.ui.paramsgraph

import android.graphics.Color
import android.graphics.RectF
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.alpriest.energystats.models.Variable
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.Fill
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
    ): Unit {
        val labelToValueSpacing = 10f
        val backgroundPadding = 10f
        val decimalFormat = DecimalFormat("#.##;−#.##")
        val variables = context.model.extraStore[VariablesKey]

        with(context) {
            val lineTargets = targets.mapNotNull { it as? LineCartesianLayerMarkerTarget }
            var typeText = ""
            val typeMaxWidth = lineTargets
                .flatMap { it.points }
                .mapIndexed { index, point ->
                    val text = variables.getOrNull(index)?.name
                    typeText = typeText + text
                    if (index != variables.count()) {
                        typeText += "\n"
                    }
                    label.getBounds(context, text)
                }.maxOf { it.width() }

            var valueText = ""
            val valueMaxWidth = lineTargets.flatMap { it.points }.mapIndexed { index, point ->
                val text = decimalFormat.format(point.entry.y)
                valueText = valueText + text
                if (index != variables.count()) {
                    valueText += "\n"
                }
                label.getBounds(context, text)
            }.maxOf { it.width() }

            val backgroundWidth = typeMaxWidth + labelToValueSpacing + valueMaxWidth + (2 * backgroundPadding)
            val backgroundShape = ShapeComponent(
                Fill(Color.WHITE),
                strokeFill = Fill(Color.BLACK),
                strokeThicknessDp = 1.0f
            )

            val typeLabel = TextComponent(
                lineCount = variables.count()
            )
            val valueLabel = TextComponent(
                lineCount = variables.count()
            )
            val text = valueFormatter.format(context, targets)
            val targetX = targets.averageOf { it.canvasX }
            val labelBounds = typeLabel.getBounds(context, text, layerBounds.width().toInt())
            val textWidth = labelBounds.width()
            val margin = 10.0f
            val x = overrideXPositionToFit(targetX, layerBounds, textWidth, margin)
            val y: Float = context.layerBounds.top

            backgroundShape.draw(
                context,
                x,
                y,
                x + backgroundWidth,
                y + labelBounds.height()
            )

            // Draw types
            typeLabel.draw(
                context = context,
                text = typeText,
                x = x,
                y = y,
                horizontalPosition = Position.Horizontal.End,
                verticalPosition = Position.Vertical.Bottom,
            )

            // Draw values
            valueLabel.draw(
                context = context,
                text = valueText,
                x = x + typeMaxWidth,
                y = y,
                horizontalPosition = Position.Horizontal.End,
                verticalPosition = Position.Vertical.Bottom,
            )
        }
    }

    protected fun overrideXPositionToFit(
        xPosition: Float,
        bounds: RectF,
        textWidth: Float,
        margin: Float,
    ): Float =
        when {
            xPosition + textWidth > bounds.right -> xPosition - margin - textWidth
            else -> xPosition + margin
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
    private fun SpannableStringBuilder.append(target: CartesianMarker.Target, variables: List<Variable>?) {
        val lineTarget = target as? LineCartesianLayerMarkerTarget ?: return

        lineTarget.points.forEachIndexed { index, point ->
            append(variables?.getOrNull(index)?.name)
            append(decimalFormat.format(point.entry.y))
            append(variables?.getOrNull(index)?.unit)
            if (index != target.points.lastIndex) append("\n")
        }
    }

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ): CharSequence {
        val variables = context.model.extraStore.getOrNull(VariablesKey)

        return SpannableStringBuilder().apply {
            targets.forEachIndexed { index, target ->
                append(target, variables)
                if (index != targets.lastIndex) append(", ")
            }
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
