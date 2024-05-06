package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.models.isFlowing
import com.alpriest.energystats.models.power
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.EarningsView
import com.alpriest.energystats.ui.flow.EarningsViewModel
import com.alpriest.energystats.ui.flow.LineOrientation
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.flow.PowerFlowTabViewModel
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.flow.StringPower
import com.alpriest.energystats.ui.flow.battery.BatteryIconView
import com.alpriest.energystats.ui.flow.battery.BatteryPowerFlow
import com.alpriest.energystats.ui.flow.grid.GridIconView
import com.alpriest.energystats.ui.flow.grid.GridPowerFlowView
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.PowerFlowStringsSettings
import com.alpriest.energystats.ui.settings.TotalYieldModel
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.PowerFlowNeutralText
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun LoadedPowerFlowView(
    configManager: ConfigManaging,
    powerFlowViewModel: PowerFlowTabViewModel,
    loadedPowerFlowViewModel: LoadedPowerFlowViewModel = viewModel(),
    themeStream: MutableStateFlow<AppTheme>,
) {
    val iconHeight = themeStream.collectAsState().value.iconHeight()
    val theme by themeStream.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        if (theme.totalYieldModel != TotalYieldModel.Off) {
            Text(
                text = stringResource(
                    id = R.string.solarYieldToday,
                    loadedPowerFlowViewModel.todaysGeneration.solarToday().energy(theme.displayUnit, theme.decimalPlaces)
                )
            )
        }

        if (theme.showFinancialSummaryOnFlowPage) {
            EarningsView(themeStream, loadedPowerFlowViewModel.earnings)
        }

        Box(contentAlignment = Alignment.Center) {
            if (!theme.shouldCombineCT2WithPVPower) {
                CT2FlowView(iconHeight, themeStream, loadedPowerFlowViewModel)
            }

            Box(contentAlignment = Alignment.Center) {
                SolarPowerFlow(
                    loadedPowerFlowViewModel.solar,
                    modifier = Modifier
                        .fillMaxHeight(0.4f),
                    iconHeight = iconHeight * 1.1f,
                    themeStream = themeStream
                )

                if (theme.powerFlowStrings.enabled && loadedPowerFlowViewModel.solar.isFlowing()) {
                    Column(
                        modifier = Modifier
                            .offset(y = (-20).dp)
                            .background(Color.LightGray)
                            .padding(2.dp),
                    ) {
                        loadedPowerFlowViewModel.solarStrings.forEach {
                            Row {
                                Text(
                                    it.displayName(theme.powerFlowStrings),
                                    Modifier.padding(end = 4.dp),
                                    fontSize = 10.sp,
                                    color = PowerFlowNeutralText
                                )
                                Text(
                                    it.amount.power(theme.displayUnit, theme.decimalPlaces),
                                    fontSize = 10.sp,
                                    color = PowerFlowNeutralText
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            Row {
                loadedPowerFlowViewModel.batteryViewModel?.let { model ->
                    if (loadedPowerFlowViewModel.hasBattery) {
                        BatteryPowerFlow(
                            viewModel = model,
                            modifier = Modifier.weight(2f),
                            themeStream = themeStream
                        )
                        InverterSpacer(
                            modifier = Modifier.weight(1f),
                            themeStream = themeStream
                        )
                    }
                }
                HomePowerFlowView(
                    amount = loadedPowerFlowViewModel.home,
                    modifier = Modifier.weight(2f),
                    themeStream = themeStream,
                    position = if (loadedPowerFlowViewModel.hasBattery) PowerFlowLinePosition.MIDDLE else PowerFlowLinePosition.LEFT
                )
                InverterSpacer(
                    modifier = Modifier.weight(1f),
                    themeStream = themeStream
                )
                GridPowerFlowView(
                    amount = loadedPowerFlowViewModel.grid,
                    modifier = Modifier.weight(2f),
                    themeStream = themeStream
                )
            }

            InverterView(
                themeStream,
                InverterViewModel(
                    configManager,
                    temperatures = loadedPowerFlowViewModel.inverterTemperatures,
                    deviceState = loadedPowerFlowViewModel.deviceState,
                    faults = loadedPowerFlowViewModel.faults
                )
            )
        }

        Row {
            loadedPowerFlowViewModel.batteryViewModel?.let { model ->
                BatteryIconView(
                    viewModel = model,
                    themeStream = themeStream,
                    iconHeight = iconHeight,
                    modifier = Modifier
                        .weight(2f)
                        .padding(top = 4.dp)
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )
            }

            HomeIconView(
                viewModel = loadedPowerFlowViewModel,
                themeStream = themeStream,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .padding(top = 4.dp),
                iconHeight = iconHeight
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            GridIconView(
                viewModel = loadedPowerFlowViewModel,
                iconHeight = iconHeight,
                themeStream = themeStream,
                modifier = Modifier
                    .weight(2f)
                    .padding(top = 4.dp)
            )
        }

        UpdateMessage(powerFlowViewModel, themeStream)
    }
}

@Composable
private fun CT2FlowView(
    iconHeight: Dp,
    themeStream: MutableStateFlow<AppTheme>,
    loadedPowerFlowViewModel: LoadedPowerFlowViewModel
) {
    Row {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight(0.4f)
                .weight(1f)
        ) {
            CT2Icon(
                modifier = Modifier.size(width = iconHeight + 4.dp, height = iconHeight + 4.dp),
                themeStream
            )

            PowerFlowView(
                amount = loadedPowerFlowViewModel.ct2,
                themeStream = themeStream,
                position = PowerFlowLinePosition.NONE,
                orientation = LineOrientation.VERTICAL,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .fillMaxHeight(0.7f)
            )
        }

        Spacer(
            modifier = Modifier.weight(3f)
        )
    }

    Column(modifier = Modifier.fillMaxHeight(0.4f)) {
        Spacer(modifier = Modifier.fillMaxHeight(0.7f))

        Row(modifier = Modifier.fillMaxHeight(0.2f)) {
            Spacer(modifier = Modifier.weight(0.72f))

            Column(modifier = Modifier.weight(2.3f)) {
                Spacer(modifier = Modifier.height(iconHeight))

                PowerFlowView(
                    amount = loadedPowerFlowViewModel.ct2,
                    themeStream = themeStream,
                    position = PowerFlowLinePosition.NONE,
                    orientation = LineOrientation.HORIZONTAL,
                    modifier = Modifier
                        .padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.weight(3f))
        }
    }
}

@Composable
fun UpdateMessage(viewModel: PowerFlowTabViewModel, themeStream: MutableStateFlow<AppTheme>) {
    val updateState by viewModel.updateMessage.collectAsState()
    val appTheme = themeStream.collectAsState().value
    var showLastUpdateTimestamp by remember { mutableStateOf(false) }

    Row(
        Modifier
            .padding(top = 12.dp)
            .padding(bottom = 4.dp)
    ) {
        if (appTheme.showLastUpdateTimestamp) {
            Text(
                updateState.updateState.lastUpdateMessage(),
                Modifier.padding(end = 10.dp),
                color = Color.Gray,
            )

            Text(
                updateState.updateState.updateMessage(),
                color = Color.Gray
            )
        } else {
            Row(
                modifier = Modifier.clickable { showLastUpdateTimestamp = !showLastUpdateTimestamp },
            ) {
                if (showLastUpdateTimestamp) {
                    Text(
                        updateState.updateState.lastUpdateMessage(),
                        color = Color.Gray,
                    )
                } else {
                    Text(
                        updateState.updateState.updateMessage(),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 640)
@Composable
fun SummaryPowerFlowViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        LoadedPowerFlowView(
            FakeConfigManager(),
            PowerFlowTabViewModel(DemoNetworking(), FakeConfigManager(), MutableStateFlow(AppTheme.demo().copy(decimalPlaces = 3)), LocalContext.current),
            loadedPowerFlowViewModel = LoadedPowerFlowViewModel(
                solar = 1.0,
                solarStrings = listOf(
                    StringPower("PV1", 0.3),
                    StringPower("PV2", 0.7)
                ),
                home = 2.454,
                grid = 1.234,
                todaysGeneration = GenerationViewModel(response = OpenHistoryResponse(deviceSN = "1", datas = listOf()), includeCT2 = false, invertCT2 = false),
                earnings = EarningsViewModel.preview(),
                inverterTemperatures = null,
                hasBattery = true,
                battery = BatteryViewModel(),
                FakeConfigManager(),
                gridImportTotal = 1.0,
                gridExportTotal = 2.0,
                homeTotal = 1.0,
                ct2 = 0.4,
                deviceState = DeviceState.Online,
                faults = listOf()
            ),
            themeStream = MutableStateFlow(
                AppTheme.demo(
                    showInverterTemperatures = true,
                    showHomeTotal = true,
                    decimalPlaces = 3,
                    powerFlowStrings = PowerFlowStringsSettings.defaults
                )
            ),
        )
    }
}
