package com.alpriest.energystats.ui.statsgraph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.helpers.lineMarkerColor
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SelectedStatsValuesLineMarker(
    segmentCount: Int,
    model: StatsGraphLineMarkerModel,
    themeStream: MutableStateFlow<AppTheme>
) {
    val spacingBetweenBars = when (segmentCount) {
        24 -> 1.0f
        else -> 2.0f
    }

    val margin = when (segmentCount) {
        28, 29, 30, 31 -> 0f
        else -> 10f
    }

    val indexOffset = when (segmentCount) {
        24 -> 0f
        else -> 1f
    }

    val barWidth = (model.bounds.width() / segmentCount.toFloat()) - (spacingBetweenBars * 2.0f)

    // Which bar index is canvasX inside?
    val barIndex = model.x.toFloat() - indexOffset

    // Left/right of the snapped bar
    val left = margin + (barIndex * barWidth) + spacingBetweenBars

    val color = lineMarkerColor(isDarkMode(themeStream))

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color.copy(alpha = 0.4f),
            topLeft = Offset(left, 19f),
            size = Size(width = barWidth, height = model.bounds.height())
        )
    }
}
