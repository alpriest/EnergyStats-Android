@file:OptIn(ExperimentalMaterial3Api::class)

package com.alpriest.energystats.ui.flow.home

import android.annotation.SuppressLint
import android.content.res.Configuration
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.shared.models.PowerFlowStringsSettings
import com.alpriest.energystats.shared.models.StringPower
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.WidgetDataSharer
import com.alpriest.energystats.ui.flow.BannerAlertManager
import com.alpriest.energystats.ui.flow.CurrentValues
import com.alpriest.energystats.ui.flow.EarningsView
import com.alpriest.energystats.ui.flow.LineOrientation
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.flow.PowerFlowTabViewModel
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.flow.PowerText
import com.alpriest.energystats.ui.flow.battery.BatteryIconView
import com.alpriest.energystats.ui.flow.battery.BatteryPowerFlow
import com.alpriest.energystats.ui.flow.energy
import com.alpriest.energystats.ui.flow.grid.GridIconView
import com.alpriest.energystats.ui.flow.grid.GridPowerFlowView
import com.alpriest.energystats.ui.flow.preview
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.TotalYieldModel
import com.alpriest.energystats.ui.settings.inverter.CT2DisplayMode
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
    val rawIconHeight = themeStream.collectAsState().value.iconHeight()
    val iconHeight = when (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        true -> rawIconHeight * 0.85f
        false -> rawIconHeight * 1.0f
    }

    val theme by themeStream.collectAsState()
    val deviceState = loadedPowerFlowViewModel.deviceState.collectAsState().value
    val earnings = loadedPowerFlowViewModel.earnings.collectAsState().value
    val solarTotal = loadedPowerFlowViewModel.todaysGeneration.collectAsState().value
    val faults = loadedPowerFlowViewModel.faults.collectAsState().value

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxHeight()
    ) {
        if (theme.totalYieldModel != TotalYieldModel.Off) {
            ShimmerText(
                shimmering = solarTotal == null,
                text = stringResource(
                    id = R.string.solarYieldToday,
                    (solarTotal?.todayGeneration ?: 0.0).energy(theme.displayUnit, 1)
                )
            )
        }

        if (theme.showFinancialSummaryOnFlowPage) {
            EarningsView(themeStream, earnings)
        }

        Box(contentAlignment = Alignment.Center) {
            if (theme.ct2DisplayMode == CT2DisplayMode.SeparateIcon) {
                CT2FlowView(iconHeight, themeStream, loadedPowerFlowViewModel)
            }

            Box(contentAlignment = Alignment.Center) {
                SolarPowerFlow(
                    loadedPowerFlowViewModel.solar,
                    modifier = Modifier.fillMaxHeight(0.46f),
                    iconHeight = iconHeight * 1.1f,
                    themeStream = themeStream
                )

                if (theme.ct2DisplayMode == CT2DisplayMode.SeparateIcon) {
                    Column(
                        modifier = Modifier.offset(y = 100.dp)
                    ) {
                        PowerText(
                            loadedPowerFlowViewModel.solar + loadedPowerFlowViewModel.ct2,
                            themeStream,
                            Color.LightGray,
                            PowerFlowNeutralText
                        )
                    }
                }

                SolarStringsView(configManager, themeStream, loadedPowerFlowViewModel)
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
                    deviceState = deviceState,
                    faults = faults
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
                    .fillMaxHeight(0.73f)
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

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, widthDp = 380, heightDp = 640)
@Composable
fun SummaryPowerFlowViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        LoadedPowerFlowView(
            FakeConfigManager(),
            PowerFlowTabViewModel(
                DemoNetworking(),
                FakeConfigManager(),
                MutableStateFlow(AppTheme.demo().copy(decimalPlaces = 3)),
                LocalContext.current,
                WidgetDataSharer.preview(),
                BannerAlertManager()
            ),
            loadedPowerFlowViewModel = LoadedPowerFlowViewModel(
                LocalContext.current,
                currentValuesStream = MutableStateFlow(
                    CurrentValues(
                        2.45, 2.45, null, 0.4, 1.0, listOf(
                            StringPower("pv1", 0.3),
                            StringPower("pv2", 0.7)
                        )
                    )
                ),
                hasBattery = true,
                battery = BatteryViewModel(),
                FakeConfigManager(),
                currentDevice = Device.preview(),
                network = DemoNetworking(),
                BannerAlertManager()
            ),
            themeStream = MutableStateFlow(
                AppTheme.demo(
                    showInverterTemperatures = true,
                    showHomeTotal = true,
                    decimalPlaces = 3,
                    ct2DisplayMode = CT2DisplayMode.AsPowerString,
                    powerFlowStrings = PowerFlowStringsSettings(enabled = true, pv1Enabled = true, pv2Enabled = true)
                )
            )
        )
    }
}
