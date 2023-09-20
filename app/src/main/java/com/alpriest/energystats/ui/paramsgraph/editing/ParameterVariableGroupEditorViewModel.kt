package com.alpriest.energystats.ui.paramsgraph.editing

import com.alpriest.energystats.ui.paramsgraph.ParameterGraphVariable
import kotlinx.coroutines.flow.MutableStateFlow

// TODO groups2 from configManager
class ParameterVariableGroupEditorViewModel(private val groups2: List<ParameterGroup>) {
    var variables = MutableStateFlow(previewParameterGraphVariables().sortedBy { it.type.name.lowercase() })
    val selected = MutableStateFlow(groups2.first())
    val groups = MutableStateFlow(groups2)

    fun select(group: ParameterGroup) {
        selected.value = group
    }

    fun toggle(updating: ParameterGraphVariable) {
        variables.value = variables.value.map {
            if (it.type.variable == updating.type.variable) {
                return@map it.copy(isSelected = !it.isSelected, enabled = !it.isSelected)
            }

            return@map it
        }
    }
}