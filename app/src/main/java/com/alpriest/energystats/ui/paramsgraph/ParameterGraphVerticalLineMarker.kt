package com.alpriest.energystats.ui.paramsgraph

import android.graphics.RectF
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.text.HorizontalPosition
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.marker.Marker
import kotlinx.coroutines.flow.MutableStateFlow

class ParameterGraphVerticalLineMarker(
    private var valuesAtTimeStream: MutableStateFlow<List<DateTimeFloatEntry>> = MutableStateFlow(listOf()),
    private val guideline: LineComponent?,
    private val text: TextComponent,
    private val background: ShapeComponent
) : Marker {
    override fun draw(context: DrawContext, bounds: RectF, markedEntries: List<Marker.EntryModel>, chartValuesProvider: ChartValuesProvider) {
        drawGuideline(context, bounds, markedEntries, background)

        valuesAtTimeStream.value = markedEntries.mapNotNull { it.entry as? DateTimeFloatEntry }
    }

    private fun drawGuideline(
        context: DrawContext,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
        background: ShapeComponent,
    ) {
        val backgroundPadding = 10f
        val labelToValueSpacing = 20f
        val entries = markedEntries.mapNotNull {
            it.entry as? DateTimeFloatEntry
        }

        markedEntries
            .map { it.location.x }
            .toSet()
            .forEach { x ->
                guideline?.drawVertical(
                    context,
                    bounds.top,
                    bounds.bottom,
                    x,
                )

                val labelMaxWidth = entries.maxOf {
                    text.getTextBounds(context, it.type.name).width()
                }
                val valueMaxWidth = entries.maxOf {
                    text.getTextBounds(context, it.y.toString()).width()
                }

                var currentHeight = 20f
                val backgroundWidth = labelMaxWidth + labelToValueSpacing + valueMaxWidth + (2 * backgroundPadding)
                val startX = if (x > bounds.right - backgroundWidth + (2 * backgroundPadding)) x - backgroundWidth - backgroundPadding else x + backgroundPadding

                entries.forEach {
                    text.drawText(
                        context,
                        it.type.name,
                        startX + backgroundPadding,
                        currentHeight,
                        verticalPosition = VerticalPosition.Bottom,
                        horizontalPosition = HorizontalPosition.End
                    )

                    text.drawText(
                        context,
                        it.y.toString(),
                        startX + backgroundPadding + labelMaxWidth + labelToValueSpacing,
                        currentHeight,
                        verticalPosition = VerticalPosition.Bottom,
                        horizontalPosition = HorizontalPosition.End
                    )

                    currentHeight += maxOf(text.getTextBounds(context, it.type.name).height(), text.getTextBounds(context, it.y.toString()).height())
                }

                background.draw(
                    context,
                    left = startX,
                    right = startX + backgroundWidth,
                    top = 20f,
                    bottom = currentHeight
                )
            }
    }
}