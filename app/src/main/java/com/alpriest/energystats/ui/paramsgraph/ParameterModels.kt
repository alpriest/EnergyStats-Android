package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.ui.statsgraph.GraphVariable
import java.time.LocalDate
import java.time.LocalDateTime

data class ParametersDisplayMode(
    val date: LocalDate,
    val hours: Int
)

data class ParametersGraphValue(val graphPoint: Int, val time: LocalDateTime, val value: Double, val type: RawVariable)

data class ParameterGraphVariable(
    val type: RawVariable,
    override val enabled: Boolean,
    val isSelected: Boolean
) : GraphVariable {
    override val colour: Color
        get() = type.colour()
}