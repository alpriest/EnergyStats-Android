package com.alpriest.energystats.ui.paramsgraph

import android.graphics.RectF
import java.time.LocalDateTime

data class VerticalLineMarkerModel(
    val bounds: RectF,
    val canvasX: Float,
    val x: Double,
    val time: LocalDateTime?
)