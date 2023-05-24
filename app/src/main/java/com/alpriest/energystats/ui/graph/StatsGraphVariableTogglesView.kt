package com.alpriest.energystats.ui.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.ValueUsage
import com.alpriest.energystats.models.kWh
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun StatsGraphVariableTogglesView(viewModel: StatsGraphTabViewModel, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    val graphVariables = viewModel.graphVariablesStream.collectAsState()

    Column(modifier) {
        graphVariables.value.map {
            ToggleRowView(viewModel, it, themeStream)
        }
    }
}

@Composable
private fun ToggleRowView(
    viewModel: StatsGraphTabViewModel,
    it: StatsGraphVariable,
    themeStream: MutableStateFlow<AppTheme>
) {
    val textColor = if (it.enabled) MaterialTheme.colors.onBackground else DimmedTextColor
    val appTheme = themeStream.collectAsState().value
    val fontSize = appTheme.fontSize()
    val decimalPlaces = appTheme.decimalPlaces
    val totals = viewModel.totalsStream.collectAsState().value

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .padding(bottom = 6.dp)
            .clickable {
                viewModel.toggleVisibility(it)
            }
    ) {
        Box(modifier = Modifier.padding(top = if (appTheme.useLargeDisplay) 10.dp else 4.dp)) {
            Canvas(modifier = Modifier.size(16.dp)) {
                drawCircle(
                    color = it.type.colour().copy(alpha = if (it.enabled) 1.0f else 0.5f),
                    radius = size.minDimension / 2,
                    center = Offset(size.width / 2, size.height / 2)
                )
            }
        }

        Column(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ReportVariableTitle(
                    it.type,
                    color = textColor,
                    fontSize = fontSize
                )

                Text(
                    totals[it.type]?.kWh(decimalPlaces) ?: "",
                    color = textColor,
                    fontSize = fontSize,
                )
            }

            ReportVariableDescription(
                it.type,
                color = DimmedTextColor,
                fontSize = appTheme.smallFontSize()
            )
        }
    }
}

@Composable
fun title(usage: ValueUsage): String {
    return when (usage) {
        ValueUsage.SNAPSHOT -> stringResource(R.string.power)
        ValueUsage.TOTAL -> stringResource(R.string.energy)
    }
}

@Composable
fun ReportVariableTitle(variable: ReportVariable, color: Color, fontSize: TextUnit) {
    val usage = ValueUsage.TOTAL

    Text(
        when (variable) {
            ReportVariable.FeedIn -> stringResource(R.string.feed_in, title(usage))
            ReportVariable.Generation -> stringResource(R.string.output_, title(usage))
            ReportVariable.GridConsumption -> stringResource(R.string.grid_consumption) + "energy"
            ReportVariable.ChargeEnergyToTal -> stringResource(R.string.charge) + "energy"
            ReportVariable.DischargeEnergyToTal -> stringResource(R.string.discharge) + "energy"
        },
        color = color,
        fontSize = fontSize
    )
}

@Composable
fun ReportVariableDescription(variable: ReportVariable, color: Color, fontSize: TextUnit) {
    Text(
        when (variable) {
            ReportVariable.FeedIn -> stringResource(R.string.power_being_sent_to_the_grid)
            ReportVariable.GridConsumption -> stringResource(R.string.power_coming_from_the_grid)
            ReportVariable.Generation -> stringResource(R.string.solar_battery_power_coming_through_the_inverter)
            ReportVariable.ChargeEnergyToTal -> stringResource(R.string.power_charging_the_battery)
            ReportVariable.DischargeEnergyToTal -> stringResource(R.string.power_discharging_from_the_battery)
        },
        color = color,
        fontSize = fontSize
    )
}

@Composable
@Preview(widthDp = 340)
fun StatsGraphVariableTogglesViewPreview() {
    StatsGraphVariableTogglesView(StatsGraphTabViewModel(FakeConfigManager(), DemoNetworking()), themeStream = MutableStateFlow(AppTheme.preview(useLargeDisplay = false)))
}