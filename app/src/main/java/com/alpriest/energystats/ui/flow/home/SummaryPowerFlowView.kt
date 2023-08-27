package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.House
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.models.RawData
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.models.ReportData
import com.alpriest.energystats.models.ReportResponse
import com.alpriest.energystats.models.kWh
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.flow.PowerFlowTabViewModel
import com.alpriest.energystats.ui.flow.battery.BatteryIconView
import com.alpriest.energystats.ui.flow.battery.BatteryPowerFlow
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
    val showHomeTotal = themeStream.collectAsState().value.showHomeTotal
    val fontSize: TextUnit = themeStream.collectAsState().value.fontSize()
    val decimalPlaces = themeStream.collectAsState().value.decimalPlaces

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

            InverterView(themeStream, summaryPowerFlowViewModel)
        }

        Row {
            summaryPowerFlowViewModel.batteryViewModel?.let { model ->
                BatteryIconView(
                    viewModel = model,
                    themeStream = themeStream,
                    modifier = Modifier
                        .weight(2f)
                        .padding(top = 4.dp),
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
                    .padding(top = 4.dp),
            ) {
                Icon(
                    Icons.Rounded.House,
                    contentDescription = "House",
                    modifier = Modifier
                        .size(iconHeight + 18.dp)
                        .offset(y = (-8).dp),
                    tint = MaterialTheme.colors.onBackground
                )

                if (showHomeTotal) {
                    Column(
                        modifier = Modifier.offset(y = (-18).dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            summaryPowerFlowViewModel.homeTotal.kWh(decimalPlaces),
                            fontSize = fontSize,
                        )
                        Text(
                            "Used today",
                            fontSize = fontSize,
                            color = Color.Gray,
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            GridIconView(
                iconHeight = iconHeight,
                themeStream = themeStream,
                modifier = Modifier
                    .weight(2f)
                    .padding(top = 4.dp)
            )
        }

        UpdateMessage(viewModel = powerFlowViewModel)
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
                earnings = "Earnings £2.52 · £12.28 · £89.99 · £145.99",
                report = listOf(
                    ReportResponse("loads", arrayOf(ReportData(index = 27, value = 5.0)))
                )
            ),
            themeStream = MutableStateFlow(AppTheme.preview(showInverterTemperatures = true, showHomeTotal = true)),
        )
    }
}
