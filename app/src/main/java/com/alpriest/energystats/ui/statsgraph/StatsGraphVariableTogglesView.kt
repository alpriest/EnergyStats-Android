package com.alpriest.energystats.ui.statsgraph

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.helpers.asPercent
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.shared.models.ValueUsage
import com.alpriest.energystats.shared.helpers.energy
import com.alpriest.energystats.shared.helpers.kWh
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.ToggleRowView
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

@Composable
fun StatsGraphVariableTogglesView(viewModel: StatsTabViewModel, modifier: Modifier = Modifier) {
    val graphVariables = viewModel.graphVariablesStream.collectAsState()
    val selectedValues = viewModel.valuesAtTimeStream.collectAsState().value
    val totals = viewModel.totalsStream.collectAsState()
    val appTheme = viewModel.themeStream.collectAsState().value

    Column(modifier) {
        graphVariables.value.map { graphVariable ->
            val selectedValue = selectedValues[graphVariable.type]?.firstOrNull()
            val title = when (graphVariable.type) {
                ReportVariable.FeedIn -> stringResource(R.string.feed_in) + title(ValueUsage.TOTAL)
                ReportVariable.Generation -> stringResource(R.string.output) + title(ValueUsage.TOTAL)
                ReportVariable.GridConsumption -> stringResource(R.string.grid_consumption) + title(ValueUsage.TOTAL)
                ReportVariable.ChargeEnergyToTal -> stringResource(R.string.charge) + title(ValueUsage.TOTAL)
                ReportVariable.DischargeEnergyToTal -> stringResource(R.string.discharge) + title(ValueUsage.TOTAL)
                ReportVariable.Loads -> stringResource(R.string.loads) + title(ValueUsage.TOTAL)
                ReportVariable.SelfSufficiency -> stringResource(R.string.self_sufficiency)
                ReportVariable.PvEnergyToTal -> stringResource(R.string.solar) + title(ValueUsage.TOTAL)
                ReportVariable.InverterConsumption -> stringResource(R.string.inverter_consumption)
                ReportVariable.BatterySOC -> stringResource(R.string.battery_soc)
            }

            val description = when (graphVariable.type) {
                ReportVariable.FeedIn -> stringResource(R.string.reportvariable_feedin)
                ReportVariable.GridConsumption -> stringResource(R.string.reportvariable_gridconsumption)
                ReportVariable.Generation -> stringResource(R.string.reportvariable_generation)
                ReportVariable.ChargeEnergyToTal -> stringResource(R.string.reportvariable_chargeenergytotal)
                ReportVariable.DischargeEnergyToTal -> stringResource(R.string.reportvariable_dischargeenergytotal)
                ReportVariable.Loads -> stringResource(R.string.reportvariable_loads)
                ReportVariable.SelfSufficiency -> ""
                ReportVariable.PvEnergyToTal -> stringResource(R.string.pv_energy_generated)
                ReportVariable.InverterConsumption -> stringResource(R.string.estimated_energy_used_to_power_the_inverter)
                ReportVariable.BatterySOC -> stringResource(R.string.battery_state_of_charge)
            }

            if (selectedValue == null) {
                val total = totals.value[graphVariable.type]
                val value = total?.energy(appTheme.displayUnit, 1)
                ToggleRowView(graphVariable, viewModel.themeStream, { viewModel.toggleVisibility(it) }, title, description, value, null)
            } else {
                val selectedValue = selectedValue.y.toDouble()
                val value = when (graphVariable.type) {
                    ReportVariable.SelfSufficiency, ReportVariable.BatterySOC -> (selectedValue / 100.0).asPercent()
                    else -> selectedValue.kWh(1)
                }

                ToggleRowView(graphVariable, viewModel.themeStream, { viewModel.toggleVisibility(it) }, title, description, value, null)
            }
        }
    }
}

@Composable
@Preview(widthDp = 340)
fun StatsGraphVariableTogglesViewPreview() {
    StatsGraphVariableTogglesView(
        StatsTabViewModel(
            MutableStateFlow(StatsDisplayMode.Day(LocalDate.now())),
            FakeConfigManager(),
            DemoNetworking(),
            themeStream = MutableStateFlow(AppTheme.demo())
        ) { _, _ -> null }
    )
}
