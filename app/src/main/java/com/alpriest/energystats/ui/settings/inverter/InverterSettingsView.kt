package com.alpriest.energystats.ui.settings.inverter

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.SettingsNavButton
import com.alpriest.energystats.ui.settings.SettingsCheckbox
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsScreen
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.SettingsTitleView
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun InverterSettingsView(configManager: ConfigManaging, navController: NavHostController) {
    val currentDevice = configManager.currentDevice.collectAsState()
    val showInverterTemperaturesState = rememberSaveable { mutableStateOf(configManager.showInverterTemperatures) }
    val showInverterIconState = rememberSaveable { mutableStateOf(configManager.showInverterIcon) }
    val shouldInvertCT2State = rememberSaveable { mutableStateOf(configManager.shouldInvertCT2) }
    val showInverterTypeNameState = rememberSaveable { mutableStateOf(configManager.showInverterTypeNameOnPowerflow) }
    val showInverterPlantNameState = rememberSaveable { mutableStateOf(configManager.showInverterPlantNameOnPowerflow) }
    val shouldCombineCT2WithPVPowerState = rememberSaveable { mutableStateOf(configManager.shouldCombineCT2WithPVPower) }

    SettingsPage {
        InverterChoiceView(configManager)

        currentDevice.value?.let {
            Column {
                SettingsNavButton(stringResource(R.string.manage_schedules)) { navController.navigate(SettingsScreen.InverterSchedule.name) }
            }

            SettingsColumnWithChild {
                SettingsTitleView(stringResource(R.string.display_options))
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
                    title = stringResource(R.string.show_inverter_plant_name),
                    state = showInverterPlantNameState,
                    onUpdate = { configManager.showInverterPlantNameOnPowerflow = it }
                )
            }

            SettingsColumnWithChild {
                SettingsTitleView(title = stringResource(R.string.ct2_settings))
                SettingsCheckbox(
                    title = stringResource(R.string.invert_ct2_values_when_detected),
                    state = shouldInvertCT2State,
                    onUpdate = { configManager.shouldInvertCT2 = it },
                    footer = buildAnnotatedString { append("If you have multiple inverters and your PV generation values are incorrect try toggling this.") }
                )

                SettingsCheckbox(
                    title = "Combine CT2 with PV power",
                    state = shouldCombineCT2WithPVPowerState,
                    onUpdate = { configManager.shouldCombineCT2WithPVPower = it }
                )
            }

            FirmwareVersionView(it)
            DeviceVersionView(it)
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

@Preview(widthDp = 300)
@Composable
fun InverterSettingsViewPreview() {
    EnergyStatsTheme {
        InverterSettingsView(
            FakeConfigManager(),
            NavHostController(LocalContext.current)
        )
    }
}