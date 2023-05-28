package com.alpriest.energystats.ui.paramsgraph

import com.alpriest.energystats.models.RawVariable
import java.time.LocalDate

data class ParametersDisplayMode(
    val date: LocalDate,
    val hours: Int
)

data class ParametersGraphValue(val graphPoint: Int, val value: Double, val type: RawVariable)

data class ParameterGraphVariable(
    val type: RawVariable,
    val enabled: Boolean,
    val isSelected: Boolean
)