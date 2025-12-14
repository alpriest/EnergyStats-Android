package com.alpriest.energystats.ui.statsgraph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.helpers.lineMarkerColor
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SelectedStatsValuesLineMarker(
    displayMode: StatsDisplayMode,
    model: StatsGraphLineMarkerModel,
    themeStream: MutableStateFlow<AppTheme>,
    showingStartAxis: Boolean
) {
    val leadingMargin = model.dimensions.startPadding // when startPadding, markers align with first x point
    val trailingMargin = model.dimensions.endPadding // when endPadding, markers align with last x point

    val boundsWidth = model.width - leadingMargin - trailingMargin

    val indexOffset = when (displayMode) {
        is StatsDisplayMode.Day -> 0f
        is StatsDisplayMode.Month -> -1f
        is StatsDisplayMode.Year -> -1f
        is StatsDisplayMode.Custom -> 0f
    }

    val barWidth = model.dimensions.xSpacing
//    val boundsWidth = barWidth * displayMode.segmentCount

    // Which bar index is canvasX inside?
    val barIndex = model.x.toFloat()  + indexOffset

    // Left of the snapped bar
    val left = leadingMargin + (barIndex * model.dimensions.xSpacing) - (barWidth / 2.0f)

    val color = lineMarkerColor(isDarkMode(themeStream))

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            Color.Blue.copy(alpha = 0.1f),
            topLeft = Offset(leadingMargin, 21f),
            size = Size(width = boundsWidth, height = model.height)
        )

        drawRect(
            color.copy(alpha = 0.3f),
            topLeft = Offset(left, 21f),
            size = Size(width = barWidth, height = model.height)
        )
    }
}
