package com.alpriest.energystats.ui.statsgraph

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.R
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ValueUsage
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.ToggleRowView
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun StatsGraphVariableTogglesView(viewModel: StatsGraphTabViewModel, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    val graphVariables = viewModel.graphVariablesStream.collectAsState()

    Column(modifier) {
        graphVariables.value.map {
            val title = when (it.type) {
                ReportVariable.FeedIn -> stringResource(R.string.feed_in, title(ValueUsage.TOTAL))
                ReportVariable.Generation -> stringResource(R.string.output_, title(ValueUsage.TOTAL))
                ReportVariable.GridConsumption -> stringResource(R.string.grid_consumption) + title(ValueUsage.TOTAL)
                ReportVariable.ChargeEnergyToTal -> stringResource(R.string.charge) + title(ValueUsage.TOTAL)
                ReportVariable.DischargeEnergyToTal -> stringResource(R.string.discharge) + title(ValueUsage.TOTAL)
            }

            val description = when (it.type) {
                ReportVariable.FeedIn -> stringResource(R.string.power_being_sent_to_the_grid)
                ReportVariable.GridConsumption -> stringResource(R.string.power_coming_from_the_grid)
                ReportVariable.Generation -> stringResource(R.string.solar_battery_power_coming_through_the_inverter)
                ReportVariable.ChargeEnergyToTal -> stringResource(R.string.power_charging_the_battery)
                ReportVariable.DischargeEnergyToTal -> stringResource(R.string.power_discharging_from_the_battery)
            }

            ToggleRowView(it, themeStream, { viewModel.toggleVisibility(it) }, title, description, viewModel.total(it))
        }
    }
}

@Composable
@Preview(widthDp = 340)
fun StatsGraphVariableTogglesViewPreview() {
    StatsGraphVariableTogglesView(StatsGraphTabViewModel(FakeConfigManager(), DemoNetworking()), themeStream = MutableStateFlow(AppTheme.preview(useLargeDisplay = false)))
}
