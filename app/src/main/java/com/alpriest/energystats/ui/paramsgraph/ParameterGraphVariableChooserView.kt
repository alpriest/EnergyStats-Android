package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.ui.settings.RoundedColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsTitleView
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ParameterGraphVariableChooserViewModel(var variables: List<ParameterGraphVariable>) {
    private var _variablesState = MutableStateFlow(variables.sortedBy { it.type.name })
    val variablesState: StateFlow<List<ParameterGraphVariable>> = _variablesState.asStateFlow()

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

@Composable
fun ParameterGraphVariableChooserView(viewModel: ParameterGraphVariableChooserViewModel, onCancel: () -> Unit) {
    val scrollState = rememberScrollState()
    val variables = viewModel.variablesState.collectAsState().value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 44.dp)
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 94.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            RoundedColumnWithChild {
                SettingsTitleView("ALL")

                RoundedColumnWithChild {
                    SettingsTitleView("PREDEFINED SELECTIONS")

                    OutlinedButton(onClick = { viewModel.chooseDefaultVariables() }) { Text("Default") }
                    OutlinedButton(onClick = { viewModel.chooseCompareStringsValues() }) { Text("Compare strings") }
                    OutlinedButton(onClick = { viewModel.chooseTemperaturesVariables() }) { Text("Temperatures") }
                    OutlinedButton(onClick = { viewModel.chooseNoVariables() }) { Text("None") }
                }
            }

            Column {
                variables.forEach { variable ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { viewModel.toggle(variable) }
                            .fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = variable.isSelected,
                            onCheckedChange = {
                                viewModel.toggle(variable)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = colors.primary)
                        )
                        Text(variable.type.name)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onCancel() }) {
                    Text("Cancel")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { /*TODO*/ }
                ) {
                    Text("Apply")
                }
            }
            Text("Note that not all parameters contain values", modifier = Modifier.align(CenterHorizontally))
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun ParameterGraphVariableChooserViewPreview() {
    val variables = listOf(
        RawVariable("PV1Volt", "pv1Volt", "V"),
        RawVariable("PV1Current", "pv1Current", "A"),
        RawVariable("PV1Power", "pv1Power", "kW"),
        RawVariable("PVPower", "pvPower", "kW"),
        RawVariable("PV2Volt", "pv2Volt", "V"),
        RawVariable("PV2Current", "pv2Current", "A"),
        RawVariable("aPV1Current", "pv1Current", "A"),
        RawVariable("aPV1Power", "pv1Power", "kW"),
        RawVariable("aPVPower", "pvPower", "kW"),
        RawVariable("aPV2Volt", "pv2Volt", "V"),
        RawVariable("aPV2Current", "pv2Current", "A"),
        RawVariable("bPV1Current", "pv1Current", "A"),
        RawVariable("bPV1Power", "pv1Power", "kW"),
        RawVariable("bPVPower", "pvPower", "kW"),
        RawVariable("bPV2Volt", "pv2Volt", "V"),
        RawVariable("cPV2Current", "pv2Current", "A"),
        RawVariable("cPV1Current", "pv1Current", "A"),
        RawVariable("cPV1Power", "pv1Power", "kW"),
        RawVariable("cPVPower", "pvPower", "kW"),
        RawVariable("cPV2Volt", "pv2Volt", "V"),
        RawVariable("dPV2Current", "pv2Current", "A"),
        RawVariable("dPV2Power", "pv2Power", "kW")
    ).map { variable ->
        ParameterGraphVariable(variable, isSelected = listOf(true, false).random(), enabled = true)
    }

    EnergyStatsTheme {
        ParameterGraphVariableChooserView(
            viewModel = ParameterGraphVariableChooserViewModel(variables),
            onCancel = {}
        )
    }
}