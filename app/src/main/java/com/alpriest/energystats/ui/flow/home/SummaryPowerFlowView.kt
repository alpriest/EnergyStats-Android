package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.ui.flow.battery.BatteryPowerFlow
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow

class SummaryPowerFlowView {
    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        viewModel: SummaryPowerFlowViewModel = viewModel(),
        themeStream: MutableStateFlow<AppTheme>
    ) {
        val iconHeight = if (themeStream.collectAsState().value == AppTheme.UseDefaultDisplay) 40.dp else 80.dp

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
                    iconHeight = iconHeight,
                    themeStream = themeStream
                )
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp)
            ) {
                BatteryPowerFlow(
                    viewModel = viewModel.batteryViewModel,
                    iconHeight = iconHeight,
                    modifier = Modifier.weight(2f),
                    themeStream = themeStream
                )
                InverterSpacer(
                    modifier = Modifier.weight(1f),
                    themeStream = themeStream
                )
                HomePowerFlowView(
                    amount = viewModel.home,
                    modifier = Modifier.weight(2f),
                    iconHeight = iconHeight,
                    themeStream = themeStream
                )
                InverterSpacer(
                    modifier = Modifier.weight(1f),
                    themeStream = themeStream
                )
                GridPowerFlowView(
                    amount = viewModel.grid,
                    modifier = Modifier.weight(2f),
                    iconHeight = iconHeight,
                    themeStream = themeStream
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 500, heightDp = 800)
@Composable
fun SummaryPowerFlowViewPreview() {
    EnergyStatsTheme {
        Box(modifier = Modifier.height(600.dp)) {
            SummaryPowerFlowView().Content(
                viewModel = SummaryPowerFlowViewModel(
                    FakeConfigManager(),
                    2.3,
                    20.5,
                    0.03,
                    0.0,
                    0.4,
                    true
                ),
                themeStream = MutableStateFlow(AppTheme.UseLargeDisplay)
            )
        }
    }
}
