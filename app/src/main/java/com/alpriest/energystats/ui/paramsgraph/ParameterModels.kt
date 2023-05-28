package com.alpriest.energystats.ui.paramsgraph

import com.alpriest.energystats.models.RawVariable
import java.time.LocalDate

data class ParametersDisplayMode(
    val date: LocalDate,
    val hours: Int
)

data class ParametersGraphValue(val graphPoint: Int, val value: Double, val type: RawVariable)

data class ParametersGraphVariable(
    val type: RawVariable,
    var enabled: Boolean,
    var isSelected: Boolean
) {
    val id: String
        get() = type.title()

    fun setIsSelected(selected: Boolean) {
        isSelected = selected
        enabled = selected
    }
}
