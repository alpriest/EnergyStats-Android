package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.House
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.models.RawData
import com.alpriest.energystats.models.RawDataStore
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.flow.PowerFlowTabViewModel
import com.alpriest.energystats.ui.flow.battery.BatteryIconView
import com.alpriest.energystats.ui.flow.battery.BatteryPowerFlow
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SummaryPowerFlowView(
    powerFlowViewModel: PowerFlowTabViewModel,
    summaryPowerFlowViewModel: SummaryPowerFlowViewModel = viewModel(),
    themeStream: MutableStateFlow<AppTheme>
) {
    val iconHeight = themeStream.collectAsState().value.iconHeight()
    val density = LocalDensity.current
    val minimumHeightState = remember { MinimumHeightState() }
    val minimumHeightStateModifier = Modifier.minimumHeightModifier(
        minimumHeightState,
        density
    )
    val updateState by powerFlowViewModel.updateMessage.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
        ) {
            SolarPowerFlow(
                summaryPowerFlowViewModel.solar,
                modifier = Modifier
                    .width(70.dp)
                    .weight(1f),
                iconHeight = iconHeight,
                themeStream = themeStream
            )
        }

        Row(
            modifier = Modifier
                .fillMaxHeight()
                .weight(3f)
        ) {
            BatteryPowerFlow(
                viewModel = summaryPowerFlowViewModel.batteryViewModel,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2f),
                themeStream = themeStream
            )
            InverterSpacer(
                modifier = Modifier.weight(1f),
                themeStream = themeStream
            )
            HomePowerFlowView(
                amount = summaryPowerFlowViewModel.home,
                modifier = Modifier.weight(2f),
                themeStream = themeStream
            )
            InverterSpacer(
                modifier = Modifier.weight(1f),
                themeStream = themeStream
            )
            GridPowerFlowView(
                amount = summaryPowerFlowViewModel.grid,
                modifier = Modifier.weight(2f),
                themeStream = themeStream
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
        ) {
            BatteryIconView(
                viewModel = summaryPowerFlowViewModel.batteryViewModel,
                themeStream = themeStream,
                modifier = minimumHeightStateModifier.weight(2f),
                iconHeight = iconHeight
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = minimumHeightStateModifier
                    .fillMaxWidth()
                    .weight(2f)
            ) {
                Icon(
                    Icons.Rounded.House,
                    contentDescription = "House",
                    modifier = Modifier.size(iconHeight),
                    tint = MaterialTheme.colors.onBackground
                )
            }
            Spacer(
                modifier = Modifier.weight(1f)
            )

            GridIconView(
                iconHeight = iconHeight,
                themeStream = themeStream,
                modifier = minimumHeightStateModifier.weight(2f)
            )
        }

        Text(
            updateState.updateState.toString2(),
            color = Color.Gray,
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(bottom = 4.dp),
        )
    }
}

fun Modifier.minimumHeightModifier(state: MinimumHeightState, density: Density) = onSizeChanged { size ->
    val itemHeight = with(density) {
        val height = size.height
        height.toDp()
    }

    if (itemHeight > (state.minHeight ?: 0.dp)) {
        state.minHeight = itemHeight
    }
}.defaultMinSize(minHeight = state.minHeight ?: Dp.Unspecified)

class MinimumHeightState(minHeight: Dp? = null) {
    var minHeight by mutableStateOf(minHeight)
}

@Preview(showBackground = true, widthDp = 500, heightDp = 600)
@Composable
fun SummaryPowerFlowViewPreview() {
    val now = SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date())

    EnergyStatsTheme {
        Box(modifier = Modifier.height(600.dp)) {
            SummaryPowerFlowView(
                PowerFlowTabViewModel(DemoNetworking(), FakeConfigManager(), RawDataStore()),
                summaryPowerFlowViewModel = SummaryPowerFlowViewModel(
                    FakeConfigManager(),
                    2.3,
                    0.5,
                    true,
                    raw = listOf(
                        RawResponse("feedInPower", arrayListOf(RawData(now, 2.45))),
                        RawResponse("generationPower", arrayListOf(RawData(now, 2.45))),
                        RawResponse("batChargePower", arrayListOf(RawData(now, 2.45))),
                        RawResponse("batDischargePower", arrayListOf(RawData(now, 2.45))),
                        RawResponse("gridConsumptionPower", arrayListOf(RawData(now, 2.45))),
                        RawResponse("loadsPower", arrayListOf(RawData(now, 2.45)))
                    ),
                    13.6
                ),
                themeStream = MutableStateFlow(AppTheme.preview()),
            )
        }
    }
}
