package com.alpriest.energystats.ui.paramsgraph

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.alpriest.energystats.ui.theme.AppTheme
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.model.Point
import kotlinx.coroutines.flow.MutableStateFlow

data class ParameterGraphVerticalLineMarkerModel(
    val context: DrawContext,
    val bounds: RectF,
    val location: Point,
    val markedEntries: List<ChartEntry>
)

@Composable
fun SelectedParameterValuesLineMarker(
    allEntries: List<List<ChartEntry>>,
    model: ParameterGraphVerticalLineMarkerModel,
    themeStream: MutableStateFlow<AppTheme>
) {
    val backgroundPadding = 10f
    val labelToValueSpacing = 10f
    val labelToBackgroundLeadPadding = 5f
    val time = model.markedEntries.firstNotNullOfOrNull { it as? DateTimeFloatEntry }?.localDateTime
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(fontSize = 12.sp, color = Color.Black)
    val entries = allEntries.flatMap { list -> list.mapNotNull { it as? DateTimeFloatEntry }.filter { it.localDateTime == time } }
    val decimalPlaces = themeStream.collectAsState().value.decimalPlaces

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {}  // Prevents additional gestures from interfering
    ) {
        val x = model.location.x
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
            textMeasurer.measure(it.formattedValue(decimalPlaces)).size.width
        }

        var currentHeight = 20f
        val backgroundWidth = labelMaxWidth + labelToValueSpacing + valueMaxWidth + (2 * backgroundPadding)
        val startX = if (x > model.bounds.right - backgroundWidth + (2 * backgroundPadding)) x - backgroundWidth - backgroundPadding else x + backgroundPadding

        entries.forEach {
            currentHeight += maxOf(
                textMeasurer.measure(it.type.name, style = textStyle).size.height,
                textMeasurer.measure(it.formattedValue(decimalPlaces), style = textStyle).size.height
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
                topLeft = Offset(
                    x = startX + labelToBackgroundLeadPadding,
                    y = currentHeight + labelToBackgroundLeadPadding
                ),
            )

            val valueResult = textMeasurer.measure(
                it.formattedValue(decimalPlaces),
                style = textStyle,
            )
            drawText(
                valueResult,
                Color.Black,
                Offset(
                    x = startX + backgroundWidth - valueResult.size.width - backgroundPadding,
                    y = currentHeight + labelToBackgroundLeadPadding
                )
            )

            currentHeight += maxOf(
                textMeasurer.measure(it.type.name, style = textStyle).size.height,
                textMeasurer.measure(it.formattedValue(decimalPlaces), style = textStyle).size.height
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

class ParameterGraphVerticalLineMarker(
    private var allProducers: Map<String, Pair<ChartEntryModelProducer, AxisScale>>,
    private var valuesAtTimeStream: MutableStateFlow<List<DateTimeFloatEntry>> = MutableStateFlow(listOf()),
    private var lastMarkerModel: MutableStateFlow<ParameterGraphVerticalLineMarkerModel?>
) : Marker {
    override fun draw(context: DrawContext, bounds: RectF, markedEntries: List<Marker.EntryModel>, chartValuesProvider: ChartValuesProvider) {
        markedEntries.firstOrNull()?.let { entryModel ->
            val allMarkedEntries: List<ChartEntry> = allProducers.values
                .flatMap { (producer, _) -> producer.getModel()?.entries?.flatten() ?: listOf() }
                .filter { it.x == entryModel.entry.x }

            lastMarkerModel.value = ParameterGraphVerticalLineMarkerModel(context, bounds, entryModel.location, allMarkedEntries)

            valuesAtTimeStream.value = allMarkedEntries.mapNotNull { it as? DateTimeFloatEntry }
        }
    }
}