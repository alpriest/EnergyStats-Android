package com.alpriest.energystats.ui.paramsgraph.editing

import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphVariable
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

class ParameterVariableGroupEditorViewModel(val configManager: ConfigManaging, variables: MutableStateFlow<List<ParameterGraphVariable>>) {
    var variables = MutableStateFlow(variables.value.sortedBy { it.type.name.lowercase() })
    val selected = MutableStateFlow(configManager.parameterGroups.first())
    val groups = MutableStateFlow(configManager.parameterGroups)
    val canDelete = MutableStateFlow<Boolean>(false)

    init {
        updateVariables(selected.value)
    }

    fun select(group: ParameterGroup) {
        selected.value = group
        canDelete.value = ParameterGroup.defaults.all { it.id != group.id }
        updateVariables(group)
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

    fun save() {
        val selectedGroup = selected.value

        groups.value = groups.value.map { existingGroup ->
            if (existingGroup.title == selectedGroup.title) {
                ParameterGroup(id = selectedGroup.id,
                    title = selectedGroup.title,
                    parameterNames = variables.value.filter { it.isSelected }.map { it.type.variable }
                )
            } else {
                existingGroup
            }
        }

        configManager.parameterGroups = groups.value
    }

    fun create(title: String) {
        groups.value = groups.value.plus(
            ParameterGroup(
                id = UUID.randomUUID().toString(),
                title = title,
                parameterNames = variables.value.filter { it.isSelected }.map { it.type.variable }
            )
        )

        configManager.parameterGroups = groups.value
        select(groups.value.firstOrNull { it.title == title } ?: groups.value.first())
    }

    fun delete() {
        groups.value = groups.value.filter { it.id != selected.value.id }
        configManager.parameterGroups = groups.value
        select(groups.value.first())
    }

    private fun updateVariables(group: ParameterGroup) {
        variables.value = configManager.variables.map { rawVariable ->
            ParameterGraphVariable(rawVariable, isSelected = group.parameterNames.contains(rawVariable.variable), enabled = true)
        }.sortedBy { it.type.name.lowercase() }
    }
}