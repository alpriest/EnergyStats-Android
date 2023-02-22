package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.ui.flow.battery.BatteryPowerFlow
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

class SummaryPowerFlowView {
    private val iconHeight = 40.dp

    @Composable
    fun Content(modifier: Modifier = Modifier, viewModel: SummaryPowerFlowViewModel = viewModel()) {
        val powerFlowWidth = 90.dp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                SolarPowerFlow(
                    viewModel.solar,
                    modifier = Modifier
                        .width(70.dp)
                        .weight(1f),
                    iconHeight = iconHeight
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp + (powerFlowWidth / 2.dp).dp)
            ) {
                Inverter(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.dp)
                        .weight(5f)
                )
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp)
            ) {
                BatteryPowerFlow(
                    viewModel = viewModel.batteryViewModel,
                    modifier = Modifier
                        .width(powerFlowWidth),
                    iconHeight = iconHeight
                )
                Spacer(modifier = Modifier.weight(1f))
                HomePowerFlowView(
                    amount = viewModel.home,
                    modifier = Modifier.width(powerFlowWidth),
                    iconHeight = iconHeight
                )
                Spacer(modifier = Modifier.weight(1f))
                GridPowerFlowView(
                    amount = viewModel.grid,
                    modifier = Modifier.width(powerFlowWidth),
                    iconHeight = iconHeight
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SummaryPowerFlowViewPreview() {
    EnergyStatsTheme {
        Box(modifier = Modifier.height(600.dp)) {
            SummaryPowerFlowView().Content(
                viewModel = SummaryPowerFlowViewModel(
                    FakeConfigManager(),
                    2.3,
                    0.5,
                    0.03,
                    0.0,
                    0.4,
                    true
                )
            )
        }
    }
}
