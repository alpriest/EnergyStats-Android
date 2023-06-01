package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.ToggleRowView
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ParameterGraphVariableTogglesView(viewModel: ParametersGraphTabViewModel, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    val graphVariables = viewModel.graphVariablesStream.collectAsState()
    val totals = viewModel.totalsStream.collectAsState()

    Column(modifier) {
        graphVariables.value.filter {it.isSelected}.map {
            val title = when (it.type.variable) {
                "generationPower" -> "Output power"
                "feedinPower" -> "Feed-in power"
                "batChargePower" -> "Charge power"
                "batDischargePower" -> "Discharge power"
                "gridConsumptionPower" -> "Grid consumption power"
                "loadsPower" -> "Loads power"
                else -> it.type.name
            }

            val description = when (it.type.variable) {
                "generationPower" -> "Solar / Battery power coming through the inverter"
                "feedInPower" -> "Power being sent to the grid"
                "batChargePower" -> "Power charging the battery"
                "batDischargePower" -> "Power discharging from the battery"
                "gridConsumptionPower" -> "Power coming from the grid"
                "loadsPower" -> "Loads power"
                else -> it.type.name
            }

            val total = totals.value[it.type]
            ToggleRowView(it, themeStream, { viewModel.toggleVisibility(it) }, title, description, total)
        }
    }
}

@Composable
@Preview(widthDp = 340)
fun ParameterGraphVariableTogglesViewPreview() {
    ParameterGraphVariableTogglesView(ParametersGraphTabViewModel(FakeConfigManager(), DemoNetworking()), themeStream = MutableStateFlow(AppTheme.preview(useLargeDisplay = false)))
}
