package com.alpriest.energystats.ui.paramsgraph.editing

import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphVariable
import kotlinx.coroutines.flow.MutableStateFlow

class ParameterVariableGroupEditorViewModel(val configManager: ConfigManaging, variables: MutableStateFlow<List<ParameterGraphVariable>>,) {
    var variables = MutableStateFlow(variables.value.sortedBy { it.type.name.lowercase() })
    val selected = MutableStateFlow(configManager.parameterGroups.first())
    val groups = MutableStateFlow(configManager.parameterGroups)

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

    fun rename(title: String) {
        val selectedId = selected.value.id

        groups.value = groups.value.map { existingGroup ->
                if (existingGroup.id == selectedId) {
                    ParameterGroup(id = selectedId, title = title, parameterNames = selected.value.parameterNames)
                } else {
                    existingGroup
                }
        }

        selected.value = groups.value.first { it.id == selectedId }
    }
}