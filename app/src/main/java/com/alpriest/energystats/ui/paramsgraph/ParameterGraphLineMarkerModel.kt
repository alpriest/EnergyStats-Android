package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.ui.geometry.Rect
import java.time.LocalDateTime

data class ParameterGraphLineMarkerModel(
    val bounds: Rect,
    val canvasX: Float,
    val x: Double,
    val time: LocalDateTime?
)