package com.alpriest.energystats.ui.paramsgraph.editing

import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphVariable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ParameterGroup(val title: String, val variables: List<String>) {
    companion object {
        val defaults: List<ParameterGroup>
            get() {
                return listOf(
                    ParameterGroup(
                        "Compare strings", listOf(
                            "pv1Power",
                            "pv2Power",
                            "pv3Power",
                            "pv4Power"
                        )
                    ),
                    ParameterGroup(
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

class ParameterGraphVariableChooserViewModel(val configManager: ConfigManaging, var variables: List<ParameterGraphVariable>, val onApply: (List<ParameterGraphVariable>) -> Unit) {
    private var _variablesState = MutableStateFlow(variables.sortedBy { it.type.name.lowercase() })
    val variablesState: StateFlow<List<ParameterGraphVariable>> = _variablesState.asStateFlow()
    val groups = MutableStateFlow(configManager.parameterGroups)

    fun apply() {
        onApply(variablesState.value)
    }

    fun chooseDefaultVariables() {
        select(DefaultGraphVariables)
    }

    fun chooseNoVariables() {
        select(listOf())
    }

    fun select(newVariables: List<String>) {
        _variablesState.value = _variablesState.value.map {
            val select = newVariables.contains(it.type.variable)
            return@map it.copy(isSelected = select, enabled = select)
        }
    }

    fun toggle(updating: ParameterGraphVariable) {
        _variablesState.value = _variablesState.value.map {
            if (it.type.variable == updating.type.variable) {
                return@map it.copy(isSelected = !it.isSelected, enabled = !it.isSelected)
            }

            return@map it
        }
    }

    companion object {
        val DefaultGraphVariables = listOf(
            "generationPower",
            "batChargePower",
            "batDischargePower",
            "feedinPower",
            "gridConsumptionPower"
        )
    }
}