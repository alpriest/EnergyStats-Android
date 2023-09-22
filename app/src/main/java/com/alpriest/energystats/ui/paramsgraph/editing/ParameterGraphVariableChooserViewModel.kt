package com.alpriest.energystats.ui.paramsgraph.editing

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.PowerFlowTabViewModel
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphVariable
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

data class ParameterGroup(val id: String, val title: String, val parameterNames: List<String>) {
    companion object {
        val defaults: List<ParameterGroup>
            get() {
                return listOf(
                    ParameterGroup(
                        id = UUID.randomUUID().toString(),
                        "Compare strings", listOf(
                            "pv1Power",
                            "pv2Power",
                            "pv3Power",
                            "pv4Power"
                        )
                    ),
                    ParameterGroup(
                        id = UUID.randomUUID().toString(),
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
                        id = UUID.randomUUID().toString(),
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
    private val variables: List<ParameterGraphVariable>,
    private val onApply: (List<ParameterGraphVariable>) -> Unit
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ParameterGraphVariableChooserViewModel(configManager, variables, onApply) as T
    }
}

class ParameterGraphVariableChooserViewModel(val configManager: ConfigManaging, var variables: List<ParameterGraphVariable>, val onApply: (List<ParameterGraphVariable>) -> Unit): ViewModel() {
    val variablesState: MutableStateFlow<List<ParameterGraphVariable>> = MutableStateFlow(variables.sortedBy { it.type.name.lowercase() })
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
        variablesState.value = variablesState.value.map {
            val select = newVariables.contains(it.type.variable)
            return@map it.copy(isSelected = select, enabled = select)
        }
    }

    fun toggle(updating: ParameterGraphVariable) {
        variablesState.value = variablesState.value.map {
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