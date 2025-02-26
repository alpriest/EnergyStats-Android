package com.alpriest.energystats.ui.paramsgraph

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.marker.Marker
import kotlinx.coroutines.flow.MutableStateFlow

data class ParameterGraphVerticalLineMarkerModel(
    val context: DrawContext,
    val bounds: RectF,
    val markedEntries: List<Marker.EntryModel>
)

@Composable
fun SelectedParameterValuesLineMarker(allEntries: List<List<ChartEntry>>, model: ParameterGraphVerticalLineMarkerModel) {
    val backgroundPadding = 10f
    val labelToValueSpacing = 10f
    val labelToBackgroundLeadPadding = 5f
    val time = model.markedEntries.firstNotNullOfOrNull { it.entry as? DateTimeFloatEntry }?.localDateTime
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(fontSize = 12.sp, color = Color.Black)
    val entries = allEntries.flatMap { list -> list.mapNotNull { it as? DateTimeFloatEntry }.filter { it.localDateTime == time } }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {}  // Prevents additional gestures from interfering
    ) {
        model.markedEntries
            .map { it.location.x }
            .toSet()
            .forEach { x ->
                drawLine(
                    color = Color.Red,
                    start = Offset(x, model.bounds.top),
                    end = Offset(x, model.bounds.bottom),
                    strokeWidth = 2.0f
                )

                val labelMaxWidth = entries.maxOf {
                    textMeasurer.measure(it.type.name).size.width
                }
                val valueMaxWidth = entries.maxOf {
                    textMeasurer.measure(it.y.toString()).size.width
                }

                var currentHeight = 20f
                val backgroundWidth = labelMaxWidth + labelToValueSpacing + valueMaxWidth + (2 * backgroundPadding)
                val startX = if (x > model.bounds.right - backgroundWidth + (2 * backgroundPadding)) x - backgroundWidth - backgroundPadding else x + backgroundPadding

                entries.forEach {
                    currentHeight += maxOf(
                        textMeasurer.measure(it.type.name, style = textStyle).size.height,
                        textMeasurer.measure(it.y.toString(), style = textStyle).size.height
                    )
                }

                drawRect(
                    Color.White.copy(alpha = 0.6f),
                    topLeft = Offset(startX, 20f),
                    size = Size(width = backgroundWidth, currentHeight - 15f)
                )

                currentHeight = 20f

                entries.forEach {
                    val typeNameResult = textMeasurer.measure(
                        it.type.name,
                        style = textStyle,
                    )
                    drawText(
                        typeNameResult,
                        Color.Black,
                        topLeft = Offset(x = startX + labelToBackgroundLeadPadding, y = currentHeight + labelToBackgroundLeadPadding),
                    )

                    val valueResult = textMeasurer.measure(
                        it.y.toString(),
                        style = textStyle,
                    )
                    drawText(
                        valueResult,
                        Color.Black,
                        Offset(x = startX + backgroundPadding + labelMaxWidth + labelToValueSpacing, y = currentHeight + labelToBackgroundLeadPadding)
                    )

                    currentHeight += maxOf(
                        textMeasurer.measure(it.type.name, style = textStyle).size.height,
                        textMeasurer.measure(it.y.toString(), style = textStyle).size.height
                    )
                }

                drawRect(
                    color = Color.Black, // Border color
                    topLeft = Offset(startX, 20f),
                    size = Size(width = backgroundWidth, height = currentHeight - 15f),
                    style = Stroke(width = 1f) // 1-pixel border
                )
            }
    }
}

class ParameterGraphVerticalLineMarker(
    private var valuesAtTimeStream: MutableStateFlow<List<DateTimeFloatEntry>> = MutableStateFlow(listOf()),
    private var lastMarkerModel: MutableStateFlow<ParameterGraphVerticalLineMarkerModel?>
) : Marker {
    override fun draw(context: DrawContext, bounds: RectF, markedEntries: List<Marker.EntryModel>, chartValuesProvider: ChartValuesProvider) {
        lastMarkerModel.value = ParameterGraphVerticalLineMarkerModel(context, bounds, markedEntries)

        valuesAtTimeStream.value = markedEntries.mapNotNull { it.entry as? DateTimeFloatEntry }
    }
}