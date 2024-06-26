package com.alpriest.energystats.ui.statsgraph

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.R
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ValueUsage
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.ToggleRowView
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun StatsGraphVariableTogglesView(viewModel: StatsTabViewModel, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    val graphVariables = viewModel.graphVariablesStream.collectAsState()
    val totals = viewModel.totalsStream.collectAsState()
    val theme = themeStream.collectAsState().value

    Column(modifier) {
        graphVariables.value.map { it ->
            val title = when (it.type) {
                ReportVariable.FeedIn -> stringResource(R.string.feed_in) + title(ValueUsage.TOTAL)
                ReportVariable.Generation -> stringResource(R.string.output) + title(ValueUsage.TOTAL)
                ReportVariable.GridConsumption -> stringResource(R.string.grid_consumption) + title(ValueUsage.TOTAL)
                ReportVariable.ChargeEnergyToTal -> stringResource(R.string.charge) + title(ValueUsage.TOTAL)
                ReportVariable.DischargeEnergyToTal -> stringResource(R.string.discharge) + title(ValueUsage.TOTAL)
                ReportVariable.Loads -> stringResource(R.string.loads) + title(ValueUsage.TOTAL)
                ReportVariable.SelfSufficiency -> stringResource(R.string.self_sufficiency)
            }

            val description = when (it.type) {
                ReportVariable.FeedIn -> stringResource(R.string.reportvariable_feedin)
                ReportVariable.GridConsumption -> stringResource(R.string.reportvariable_gridconsumption)
                ReportVariable.Generation -> stringResource(R.string.reportvariable_generation)
                ReportVariable.ChargeEnergyToTal -> stringResource(R.string.reportvariable_chargeenergytotal)
                ReportVariable.DischargeEnergyToTal -> stringResource(R.string.reportvariable_dischargeenergytotal)
                ReportVariable.Loads -> stringResource(R.string.reportvariable_loads)
                ReportVariable.SelfSufficiency -> ""
            }

            val total = totals.value[it.type]
            val text = total?.energy(theme.displayUnit, theme.decimalPlaces)
            ToggleRowView(it, themeStream, { viewModel.toggleVisibility(it) }, title, description, text, null)
        }
    }
}

@Composable
@Preview(widthDp = 340)
fun StatsGraphVariableTogglesViewPreview() {
    StatsGraphVariableTogglesView(
        StatsTabViewModel(FakeConfigManager(), DemoNetworking()) { _, _ -> null },
        themeStream = MutableStateFlow(AppTheme.demo(useLargeDisplay = false))
    )
}
