package com.alpriest.energystats.ui.paramsgraph.editing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphVariable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ParameterGraphVariableChooserViewModelFactory(
    private val configManager: ConfigManaging,
    private val variables: MutableStateFlow<List<ParameterGraphVariable>>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ParameterGraphVariableChooserViewModel(configManager, variables) as T
    }
}

data class ParameterGraphVariableChooserViewData(
    val variables: List<ParameterGraphVariable>,
    val selectedId: String?
)

class ParameterGraphVariableChooserViewModel(val configManager: ConfigManaging, var variables: MutableStateFlow<List<ParameterGraphVariable>>) : ViewModel() {
    private val _viewDataStream = MutableStateFlow(
        ParameterGraphVariableChooserViewData(
            variables.value.sortedBy { it.type.name.lowercase() },
            null
        )
    )
    val viewDataStream: StateFlow<ParameterGraphVariableChooserViewData> = _viewDataStream

    private val _dirtyState = MutableStateFlow(false)
    val dirtyState: StateFlow<Boolean> = _dirtyState

    private var originalValue: ParameterGraphVariableChooserViewData? = null

    init {
        determineSelectedGroup()

        originalValue = viewDataStream.value
        viewModelScope.launch {
            viewDataStream.collect {
                _dirtyState.value = originalValue != it
            }
        }
    }

    fun apply() {
        variables.value = viewDataStream.value.variables
    }

    fun chooseDefaultVariables() {
        select(DefaultGraphVariables)
    }

    fun chooseNoVariables() {
        select(listOf())
    }

    fun select(newVariables: List<String>) {
        val viewData = viewDataStream.value
        _viewDataStream.value = viewDataStream.value.copy(variables = viewData.variables.map {
            val select = newVariables.contains(it.type.variable)
            return@map it.copy(isSelected = select, enabled = select)
        })
        determineSelectedGroup()
    }

    fun toggle(updating: ParameterGraphVariable) {
        val viewData = viewDataStream.value
        _viewDataStream.value = viewDataStream.value.copy(variables = viewData.variables.map {
            if (it.type.variable == updating.type.variable) {
                return@map it.copy(isSelected = !it.isSelected, enabled = !it.isSelected)
            }

            return@map it
        })
        determineSelectedGroup()
    }

    private fun determineSelectedGroup() {
        val viewData = viewDataStream.value
        val sortedViewDataVariables = viewData.variables.filter { it.isSelected }.map { it.type.variable }.sorted()

        _viewDataStream.value = viewDataStream.value.copy(
            selectedId = configManager.parameterGroups.firstOrNull { group ->
                group.parameterNames.sorted() == sortedViewDataVariables
            }?.id
        )
    }

    companion object {
        val DefaultGraphVariables = listOf(
            "invBatPower",
            "meterPower",
            "loadsPower",
            "pvPower",
            "SoC"
        )
    }
}