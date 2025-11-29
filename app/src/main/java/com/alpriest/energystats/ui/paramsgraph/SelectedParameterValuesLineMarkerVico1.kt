package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.theme.AppTheme
import com.patrykandpatrick.vico1.core.entry.ChartEntry
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SelectedParameterValuesLineMarkerVico1(
    allEntries: List<List<ChartEntry>>,
    model: ParameterGraphVerticalLineMarkerModelVico1,
    themeStream: MutableStateFlow<AppTheme>
) {
    val backgroundPadding = 10f
    val labelToValueSpacing = 10f
    val labelToBackgroundLeadPadding = 5f
    val time = model.time
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(fontSize = 12.sp, color = Color.Companion.Black)
    val entries = allEntries.flatMap { list -> list.mapNotNull { it as? DateTimeFloatEntryVico1 }.filter { it.localDateTime == time } }
    val decimalPlaces = themeStream.collectAsState().value.decimalPlaces
    val color = lineMarkerColor(isDarkMode(themeStream))

    if (entries.isEmpty()) {
        return
    }

    Canvas(modifier = Modifier.Companion.fillMaxSize()) {
        val x = model.x
        drawLine(
            color = color,
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
            Color.Companion.White.copy(alpha = 0.6f),
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
                Color.Companion.Black,
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
                Color.Companion.Black,
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
            color = Color.Companion.Black, // Border color
            topLeft = Offset(startX, 20f),
            size = Size(width = backgroundWidth, height = currentHeight - 15f),
            style = Stroke(width = 1f) // 1-pixel border
        )
    }
}