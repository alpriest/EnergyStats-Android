package com.alpriest.energystats.ui.settings.inverter

import android.content.Context
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.DeviceSettingsItem
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.SegmentedControl
import com.alpriest.energystats.ui.settings.InlineSettingsNavButton
import com.alpriest.energystats.ui.settings.SettingsCheckbox
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.SettingsScreen
import com.alpriest.energystats.ui.settings.SettingsSegmentedControl
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

enum class CT2DisplayMode(val value: Int) {
    Hidden(0),
    SeparateIcon(1),
    AsPowerString(2);

    fun title(context: Context): String {
        return when (this) {
            Hidden -> context.getString(R.string.ct2displaymode_hidden)
            SeparateIcon -> context.getString(R.string.ct2displaymode_separate_icon)
            AsPowerString -> context.getString(R.string.ct2displaymode_as_power_string)
        }
    }

    companion object {
        fun fromInt(value: Int) = CT2DisplayMode.values().firstOrNull { it.value == value } ?: Hidden
    }
}

@Composable
fun InverterSettingsView(configManager: ConfigManaging, navController: NavHostController, network: Networking, modifier: Modifier) {
    val currentDevice = configManager.currentDevice.collectAsState()
    val showInverterTemperaturesState = rememberSaveable { mutableStateOf(configManager.showInverterTemperatures) }
    val showInverterIconState = rememberSaveable { mutableStateOf(configManager.showInverterIcon) }
    val shouldInvertCT2State = rememberSaveable { mutableStateOf(configManager.shouldInvertCT2) }
    val showInverterTypeNameState = rememberSaveable { mutableStateOf(configManager.showInverterTypeNameOnPowerflow) }
    val showInverterStationNameState = rememberSaveable { mutableStateOf(configManager.showInverterStationNameOnPowerflow) }
    val shouldCombineCT2WithPVPowerState = rememberSaveable { mutableStateOf(configManager.shouldCombineCT2WithPVPower) }
    val shouldCombineCT2WithPVLoadsState = rememberSaveable { mutableStateOf(configManager.shouldCombineCT2WithLoadsPower) }
    val showInverterScheduleQuickLinkState = rememberSaveable { mutableStateOf(configManager.showInverterScheduleQuickLink) }
    val ct2DisplayModeState = rememberSaveable { mutableStateOf(configManager.ct2DisplayMode) }
    val showInverterConsumptionState = rememberSaveable { mutableStateOf(configManager.showInverterConsumption) }
    val context = LocalContext.current

    trackScreenView("Inverter", "InverterSettingsView")

    SettingsPage(modifier) {
        InverterChoiceView(configManager)

        currentDevice.value?.let { device ->
            SettingsColumn {
                InlineSettingsNavButton(stringResource(R.string.manage_schedules)) { navController.navigate(SettingsScreen.InverterSchedule.name) }

                HorizontalDivider()

                InlineSettingsNavButton(DeviceSettingsItem.ExportLimit.title(context)) {
                    navController.navigate(SettingsScreen.ConfigureExportLimit.name)
                }

                HorizontalDivider()

                InlineSettingsNavButton(stringResource(R.string.peak_shaving)) {
                    navController.navigate(SettingsScreen.ConfigurePeakShaving.name)
                }

                HorizontalDivider()

                InlineSettingsNavButton(stringResource(R.string.work_mode)) {
                    navController.navigate(SettingsScreen.ConfigureWorkMode.name)
                }
            }

            SettingsColumn(header = stringResource(R.string.display_options)) {
                SettingsCheckbox(
                    title = stringResource(R.string.show_inverter_temperatures),
                    state = showInverterTemperaturesState,
                    onUpdate = { configManager.showInverterTemperatures = it }
                )

                SettingsCheckbox(
                    title = stringResource(R.string.show_inverter_icon),
                    state = showInverterIconState,
                    onUpdate = { configManager.showInverterIcon = it }
                )

                SettingsCheckbox(
                    title = stringResource(R.string.show_inverter_type_name),
                    state = showInverterTypeNameState,
                    onUpdate = { configManager.showInverterTypeNameOnPowerflow = it }
                )

                SettingsCheckbox(
                    title = stringResource(R.string.show_inverter_station_name),
                    state = showInverterStationNameState,
                    onUpdate = { configManager.showInverterStationNameOnPowerflow = it }
                )

                SettingsCheckbox(
                    title = stringResource(R.string.show_schedule_quick_link),
                    state = showInverterScheduleQuickLinkState,
                    onUpdate = { configManager.showInverterScheduleQuickLink = it }
                )

                SettingsCheckbox(
                    title = stringResource(R.string.show_estimated_inverter_consumption),
                    state = showInverterConsumptionState,
                    onUpdate = { configManager.showInverterConsumption = it }
                )
            }

            SettingsColumn(
                header = stringResource(R.string.ct2_settings),
                footer = stringResource(R.string.if_you_have_multiple_inverters_and_your_pv_generation_values_are_incorrect_try_toggling_this)
            ) {
                SettingsCheckbox(
                    title = stringResource(R.string.invert_ct2_values_when_detected),
                    state = shouldInvertCT2State,
                    onUpdate = { configManager.shouldInvertCT2 = it },
                )

                SettingsCheckbox(
                    title = stringResource(R.string.combine_ct2_with_pv_power),
                    state = shouldCombineCT2WithPVPowerState,
                    onUpdate = { configManager.shouldCombineCT2WithPVPower = it }
                )

                SettingsCheckbox(
                    title = stringResource(R.string.combine_ct2_with_loads_power),
                    state = shouldCombineCT2WithPVLoadsState,
                    onUpdate = { configManager.shouldCombineCT2WithLoadsPower = it }
                )

                SettingsSegmentedControl(
                    title = "CT2 display",
                    segmentedControl = {
                        val items = listOf(
                            CT2DisplayMode.Hidden,
                            CT2DisplayMode.SeparateIcon,
                            CT2DisplayMode.AsPowerString
                        )
                        SegmentedControl(
                            items = items.map { it.title(context) },
                            defaultSelectedItemIndex = items.indexOf(ct2DisplayModeState.value),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            ct2DisplayModeState.value = items[it]
                            configManager.ct2DisplayMode = items[it]
                        }
                    })
            }

            FirmwareVersionView(device, network)
            DeviceVersionView(device)
        }
    }
}

@Composable
fun DeviceVersionView(device: Device) {
    SettingsColumnWithChild {
        SettingsRow("Station Name", device.stationName)
        SettingsRow("Device Serial No.", device.deviceSN)
        SettingsRow("Module Serial No", device.moduleSN)
        SettingsRow("Has Battery", if (device.hasBattery) "true" else "false")
        SettingsRow("Has Solar", if (device.hasPV) "true" else "false")
    }
}

@Preview(widthDp = 400)
@Composable
fun InverterSettingsViewPreview() {
    EnergyStatsTheme {
        InverterSettingsView(
            FakeConfigManager(),
            NavHostController(LocalContext.current),
            DemoNetworking(),
            Modifier
        )
    }
}