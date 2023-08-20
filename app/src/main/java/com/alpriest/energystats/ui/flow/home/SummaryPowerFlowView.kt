package com.alpriest.energystats.ui.flow.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.House
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.models.RawData
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.flow.PowerFlowTabViewModel
import com.alpriest.energystats.ui.flow.battery.BatteryIconView
import com.alpriest.energystats.ui.flow.battery.BatteryPowerFlow
import com.alpriest.energystats.ui.flow.battery.asTemperature
import com.alpriest.energystats.ui.flow.inverter.InverterView
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SummaryPowerFlowView(
    powerFlowViewModel: PowerFlowTabViewModel,
    summaryPowerFlowViewModel: SummaryPowerFlowViewModel = viewModel(),
    themeStream: MutableStateFlow<AppTheme>
) {
    val iconHeight = themeStream.collectAsState().value.iconHeight()
    val appTheme = themeStream.collectAsState().value

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        SolarPowerFlow(
            summaryPowerFlowViewModel.solar,
            summaryPowerFlowViewModel.todaysGeneration,
            summaryPowerFlowViewModel.earnings,
            modifier = Modifier.fillMaxHeight(0.4f),
            iconHeight = iconHeight * 1.1f,
            themeStream = themeStream
        )

        Box(modifier = Modifier.weight(1f)) {
            Row {
                summaryPowerFlowViewModel.batteryViewModel?.let { model ->
                    if (summaryPowerFlowViewModel.hasBattery) {
                        BatteryPowerFlow(
                            viewModel = model,
                            modifier = Modifier
                                .weight(2f),
                            themeStream = themeStream
                        )
                        InverterSpacer(
                            modifier = Modifier.weight(1f),
                            themeStream = themeStream
                        )
                    }
                }
                HomePowerFlowView(
                    amount = summaryPowerFlowViewModel.home,
                    modifier = Modifier.weight(2f),
                    themeStream = themeStream,
                    position = if (summaryPowerFlowViewModel.hasBattery) PowerFlowLinePosition.MIDDLE else PowerFlowLinePosition.LEFT
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

            if (appTheme.showInverterTemperatures) {
                summaryPowerFlowViewModel.inverterViewModel?.let {
                    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-24).dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            InverterView(
                                modifier = Modifier
                                    .width(43.dp)
                                    .height(50.dp)
                                    .padding(bottom = 4.dp)
                                    .background(colors.background)
                            )
                            Text(
                                modifier = Modifier
                                    .background(colors.background)
                                    .padding(4.dp),
                                text = it.name
                            )

                            Row(modifier = Modifier.background(colors.background)) {
                                InverterTemperatures(it)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-12.dp))
                                .background(colors.background)
                                .padding(2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row {
                                InverterTemperatures(it)

                                Text(
                                    it.name,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-24).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    InverterView(
                        modifier = Modifier
                            .width(43.dp)
                            .height(50.dp)
                            .padding(bottom = 4.dp)
                    )
                }
            }
        }

        Row {
            summaryPowerFlowViewModel.batteryViewModel?.let { model ->
                BatteryIconView(
                    viewModel = model,
                    themeStream = themeStream,
                    modifier = Modifier.weight(2f),
                    iconHeight = iconHeight
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
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
                modifier = Modifier.weight(2f)
            )
        }

        UpdateMessage(viewModel = powerFlowViewModel)
    }
}

@Composable
private fun InverterTemperatures(viewModel: InverterViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Text(
            viewModel.temperatures.ambient.asTemperature(),
            fontSize = 12.sp
        )
        Text(
            "INTERNAL",
            fontSize = 8.sp
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            viewModel.temperatures.inverter.asTemperature(),
            fontSize = 12.sp
        )
        Text(
            "EXTERNAL",
            fontSize = 8.sp
        )
    }
}

@Composable
fun UpdateMessage(viewModel: PowerFlowTabViewModel) {
    val updateState by viewModel.updateMessage.collectAsState()

    Text(
        updateState.updateState.updateMessage(),
        color = Color.Gray,
        modifier = Modifier
            .padding(top = 12.dp)
            .padding(bottom = 4.dp)
    )
}

@Preview(showBackground = true, widthDp = 380, heightDp = 640)
@Composable
fun SummaryPowerFlowViewPreview() {
    val formatter = DateTimeFormatter.ofPattern(dateFormat)
    val now = LocalDateTime.now().format(formatter)

    EnergyStatsTheme {
        SummaryPowerFlowView(
            PowerFlowTabViewModel(DemoNetworking(), FakeConfigManager(), MutableStateFlow(AppTheme.preview())),
            summaryPowerFlowViewModel = SummaryPowerFlowViewModel(
                FakeConfigManager(),
                2.3,
                0.5,
                raw = listOf(
                    RawResponse("feedInPower", arrayListOf(RawData(now, 2.45))),
                    RawResponse("generationPower", arrayListOf(RawData(now, 2.45))),
                    RawResponse("batChargePower", arrayListOf(RawData(now, 2.45))),
                    RawResponse("batDischargePower", arrayListOf(RawData(now, 2.45))),
                    RawResponse("gridConsumptionPower", arrayListOf(RawData(now, 2.45))),
                    RawResponse("loadsPower", arrayListOf(RawData(now, 2.45))),
                    RawResponse("ambientTemperation", arrayListOf(RawData(now, 2.45))),
                    RawResponse("invTemperation", arrayListOf(RawData(now, 2.45)))
                ),
                13.6,
                todaysGeneration = 1.0,
                batteryResidual = 5678,
                hasBattery = true,
                earnings = "Earnings £2.52 · £12.28 · £89.99 · £145.99"
            ),
            themeStream = MutableStateFlow(AppTheme.preview(showInverterTemperatures = true)),
        )
    }
}
