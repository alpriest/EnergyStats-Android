package com.alpriest.energystats.ui.paramsgraph

import android.graphics.RectF
import java.time.LocalDateTime

data class ParameterGraphVerticalLineMarkerModel(
    val bounds: RectF,
    val x: Float,
    val time: LocalDateTime?
)