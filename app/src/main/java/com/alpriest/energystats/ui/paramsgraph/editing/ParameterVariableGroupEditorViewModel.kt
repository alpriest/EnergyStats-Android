package com.alpriest.energystats.ui.paramsgraph.editing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphVariable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ParameterVariableGroupEditorViewData(
    val variables: List<ParameterGraphVariable>,
    val selectedGroup: ParameterGroup,
    val groups: List<ParameterGroup>,
    val canDelete: Boolean
)

class ParameterVariableGroupEditorViewModel(
    val configManager: ConfigManaging,
    variables: MutableStateFlow<List<ParameterGraphVariable>>
) : ViewModel() {

    private val _viewDataStream: MutableStateFlow<ParameterVariableGroupEditorViewData> = MutableStateFlow(
        ParameterVariableGroupEditorViewData(
            variables = variables.value.sortedBy { it.type.name.lowercase() },
            selectedGroup = configManager.parameterGroups.first(),
            groups = configManager.parameterGroups,
            false
        )
    )
    val viewDataStream: StateFlow<ParameterVariableGroupEditorViewData> = _viewDataStream

    private val _dirtyState = MutableStateFlow(false)
    val dirtyState: StateFlow<Boolean> = _dirtyState

    private var remoteValue: ParameterVariableGroupEditorViewData? = null

    init {
        updateVariables(viewDataStream.value.selectedGroup)
        remoteValue = viewDataStream.value

        viewModelScope.launch {
            viewDataStream.collect { changedViewData ->
                _dirtyState.value = remoteValue
                    ?.groups
                    ?.firstOrNull { group -> group.id == changedViewData.selectedGroup.id }
                    ?.let { originalGroup ->
                        if (originalGroup.title != changedViewData.selectedGroup.title) {
                            true
                        } else {
                            originalGroup.parameterNames.sorted() != changedViewData.variables
                                .filter { it.isSelected }
                                .map { it.type.variable }
                                .sorted()
                        }
                    }
                    ?: false
            }
        }
    }

    fun select(group: ParameterGroup) {
        _viewDataStream.value = viewDataStream.value.copy(
            selectedGroup = group,
            canDelete = ParameterGroup.defaults.all { it.id != group.id }
        )
        updateVariables(group)
    }

    fun toggle(updating: ParameterGraphVariable) {
        val viewData = viewDataStream.value
        _viewDataStream.value = viewDataStream.value.copy(
            variables = viewData.variables.map {
                if (it.type.variable == updating.type.variable) {
                    return@map it.copy(isSelected = !it.isSelected, enabled = !it.isSelected)
                }

                return@map it
            })
    }

    fun rename(title: String) {
        val viewData = viewDataStream.value
        val selectedId = viewData.selectedGroup.id

        _viewDataStream.value = viewDataStream.value.copy(
            groups = viewData.groups.map { existingGroup ->
                if (existingGroup.id == selectedId) {
                    ParameterGroup(id = selectedId, title = title, parameterNames = viewData.selectedGroup.parameterNames)
                } else {
                    existingGroup
                }
            },

            selectedGroup = viewData.groups.first { it.id == selectedId }
        )
    }

    fun save() {
        val viewData = viewDataStream.value
        val selectedGroup = viewData.selectedGroup

        _viewDataStream.value = viewDataStream.value.copy(
            groups = viewData.groups.map { existingGroup ->
                if (existingGroup.title == selectedGroup.title) {
                    ParameterGroup(
                        id = selectedGroup.id,
                        title = selectedGroup.title,
                        parameterNames = viewData.variables.filter { it.isSelected }.map { it.type.variable }
                    )
                } else {
                    existingGroup
                }
            })

        configManager.parameterGroups = viewDataStream.value.groups
    }

    fun create(title: String) {
        val viewData = viewDataStream.value
        _viewDataStream.value = viewDataStream.value.copy(
            groups = viewData.groups.plus(
                ParameterGroup(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    parameterNames = viewData.variables.filter { it.isSelected }.map { it.type.variable }
                )
            )
        )

        configManager.parameterGroups = _viewDataStream.value.groups
        select(_viewDataStream.value.groups.firstOrNull { it.title == title } ?: _viewDataStream.value.groups.first())
    }

    fun delete() {
        val viewData = viewDataStream.value
        _viewDataStream.value = viewDataStream.value.copy(
            groups = viewData.groups.filter { it.id != viewData.selectedGroup.id }
        )
        configManager.parameterGroups = _viewDataStream.value.groups
        select(_viewDataStream.value.groups.first())
    }

    private fun updateVariables(group: ParameterGroup) {
        _viewDataStream.value = viewDataStream.value.copy(
            variables = configManager.variables.map { rawVariable ->
                ParameterGraphVariable(rawVariable, isSelected = group.parameterNames.contains(rawVariable.variable), enabled = true)
            }.sortedBy { it.type.name.lowercase() }
        )
    }
}