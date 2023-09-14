package com.alpriest.energystats.ui.paramsgraph

import com.alpriest.energystats.ui.paramsgraph.ParameterGroup.Companion.defaultParameterGroups
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ParameterGroup(val title: String, val variables: List<String>) {
    companion object {
        fun defaultParameterGroups(): List<ParameterGroup> {
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

class ParameterGraphVariableChooserViewModel(var variables: List<ParameterGraphVariable>, val onApply: (List<ParameterGraphVariable>) -> Unit) {
    private var _variablesState = MutableStateFlow(variables.sortedBy { it.type.name.lowercase() })
    val variablesState: StateFlow<List<ParameterGraphVariable>> = _variablesState.asStateFlow()
    val groups = MutableStateFlow(defaultParameterGroups())

    fun apply() {
        onApply(variablesState.value)
    }

    fun chooseDefaultVariables() {
        val newVariables = DefaultGraphVariables
        select(newVariables)
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