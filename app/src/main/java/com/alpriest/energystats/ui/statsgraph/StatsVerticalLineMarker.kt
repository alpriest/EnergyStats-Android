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
    displayMode: StatsDisplayMode,
    model: StatsGraphLineMarkerModel,
    themeStream: MutableStateFlow<AppTheme>
) {
    val leadingMargin = model.dimensions.startPadding

    val indexOffset = when (displayMode) {
        is StatsDisplayMode.Day -> 0f
        is StatsDisplayMode.Month -> -1f
        is StatsDisplayMode.Year -> -1f
        is StatsDisplayMode.Custom -> 0f
    }

    val barWidth = model.dimensions.xSpacing

    // Which bar index is canvasX inside?
    val barIndex = model.x.toFloat()  + indexOffset

    // Left of the snapped bar
    val left = leadingMargin + (barIndex * model.dimensions.xSpacing) - (barWidth / 2.0f)

    val color = lineMarkerColor(isDarkMode(themeStream))

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color.copy(alpha = 0.3f),
            topLeft = Offset(left, 21f),
            size = Size(width = barWidth, height = model.height)
        )
    }
}
