package com.alpriest.energystats.ui.paramsgraph

import android.graphics.RectF
import com.patrykandpatrick.vico1.core.context.DrawContext
import com.patrykandpatrick.vico1.core.model.Point
import java.time.LocalDateTime

data class ParameterGraphVerticalLineMarkerModel(
    val context: DrawContext,
    val bounds: RectF,
    val location: Point,
    val time: LocalDateTime?
)