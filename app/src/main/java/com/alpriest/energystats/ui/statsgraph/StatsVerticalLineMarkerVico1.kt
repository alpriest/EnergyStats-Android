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
    val margin = 20.0f

    val barWidth = (model.bounds.width() / segmentCount.toFloat()) - 1.0f

    // Which bar index is canvasX inside?
    val graphX = (model.canvasX - margin).coerceAtLeast(0f)
    val barIndex = (graphX / barWidth).toInt().coerceIn(0, segmentCount - 1)

    // Left/right of the snapped bar
    val left = margin + (barIndex * barWidth) + 1.0f

    val color = lineMarkerColor(isDarkMode(themeStream))

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color.copy(alpha = 0.4f),
            topLeft = Offset(left, 19f),
            size = Size(width = barWidth, height = model.bounds.height())
        )
    }
}
