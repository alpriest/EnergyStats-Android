package com.alpriest.energystats.ui.paramsgraph

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ParameterGraphVariableChooserViewModel(var variables: List<ParameterGraphVariable>, val onApply: (List<ParameterGraphVariable>) -> Unit) {
    private var _variablesState = MutableStateFlow(variables.sortedBy { it.type.name })
    val variablesState: StateFlow<List<ParameterGraphVariable>> = _variablesState.asStateFlow()

    fun apply() {
        onApply(variablesState.value)
    }

    fun chooseDefaultVariables() {
        val newVariables = ParametersGraphTabViewModel.DefaultGraphVariables
        select(newVariables)
    }

    fun chooseCompareStringsValues() {
        select(
            listOf(
                "pv1Power",
                "pv2Power",
                "pv3Power",
                "pv4Power"
            )
        )
    }

    fun chooseTemperaturesVariables() {
        select(
            listOf(
                "ambientTemperation",
                "boostTemperation",
                "invTemperation",
                "chargeTemperature",
                "batTemperature",
                "dspTemperature"
            )
        )
    }

    fun chooseNoVariables() {
        select(listOf())
    }

    private fun select(newVariables: List<String>) {
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
}