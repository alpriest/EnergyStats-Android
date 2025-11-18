package com.alpriest.energystats.ui.paramsgraph.editing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphVariable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ParameterGroup(val id: String, val title: String, val parameterNames: List<String>) {
    companion object {
        val defaults: List<ParameterGroup>
            get() {
                return listOf(
                    ParameterGroup(
                        id = "5875390f-62d0-4373-909c-87225ad5150c",
                        "Compare strings", listOf(
                            "pv1Power",
                            "pv2Power",
                            "pv3Power",
                            "pv4Power"
                        )
                    ),
                    ParameterGroup(
                        id = "ae0a66a8-d8ba-463b-8c58-c671ba130dc9",
                        "Temperatures", listOf(
                            "ambientTemperation",
                            "boostTemperation",
                            "invTemperation",
                            "chargeTemperature",
                            "batTemperature",
                            "dspTemperature"
                        )
                    ),
                    ParameterGroup(
                        id = "f633b777-29c5-4809-a783-6038e055cc86",
                        "Battery", listOf(
                            "batTemperature",
                            "batVolt",
                            "batCurrent",
                            "SoC"
                        )
                    ),
                )
            }
    }
}

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
//    val variablesState: MutableStateFlow<List<ParameterGraphVariable>> = MutableStateFlow(variables.value.sortedBy { it.type.name.lowercase() })
//    val selectedIdState: MutableStateFlow<String?> = MutableStateFlow(null)

    private val _viewDataStream = MutableStateFlow(
        ParameterGraphVariableChooserViewData(
            variables.value.sortedBy { it.type.name.lowercase() },
            null
        )
    )
    val viewDataStream: StateFlow<ParameterGraphVariableChooserViewData> = _viewDataStream

    private val _dirtyState = MutableStateFlow(false)
    val dirtyState: StateFlow<Boolean> = _dirtyState

    private var remoteValue: ParameterGraphVariableChooserViewData? = null

    init {
        determineSelectedGroup()

        remoteValue = viewDataStream.value
        viewModelScope.launch {
            viewDataStream.collect {
                _dirtyState.value = remoteValue != it
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
        _viewDataStream.value = viewDataStream.value.copy(
            selectedId = configManager.parameterGroups.firstOrNull { group ->
                group.parameterNames.sorted() == viewData.variables.filter { it.isSelected }.map { it.type.variable }.sorted()
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