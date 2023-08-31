package com.alpriest.energystats.ui.settings.inverter

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

    SettingsPage {
        InverterChoiceView(configManager)

        currentDevice.value?.let {
            SettingsNavButton(stringResource(R.string.configure_work_mode)) { navController.navigate(SettingsScreen.InverterWorkMode.name) }

            SettingsColumnWithChild {
                SettingsCheckbox(
                    title = stringResource(R.string.show_inverter_temperatures),
                    state = showInverterTemperaturesState,
                    onConfigUpdate = { configManager.showInverterTemperatures = it }
                )

                SettingsCheckbox(
                    title = stringResource(R.string.show_inverter_icon),
                    state = showInverterIconState,
                    onConfigUpdate = { configManager.showInverterIcon = it }
                )
            }

            SettingsColumnWithChild {
                SettingsTitleView(title = stringResource(R.string.advanced))
                SettingsCheckbox(
                    title = stringResource(R.string.invert_ct2_values_when_detected),
                    state = showInverterIconState,
                    onConfigUpdate = { configManager.showInverterIcon = it },
                    footer = buildAnnotatedString { append("If you have multiple inverters and your PV generation values are incorrect try toggling this.") }
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
        SettingsRow("Plant Name", device.plantName)
        SettingsRow("Device Type", device.deviceType)
        SettingsRow("Device ID", device.deviceID)
        SettingsRow("Device Serial No.", device.deviceSN)
        SettingsRow("Module Serial No", device.moduleSN)
        SettingsRow("Has Battery", if (device.hasBattery) "true" else "false")
        SettingsRow("Has Solar", if (device.hasPV) "true" else "false")
    }
}

@Preview(widthDp = 400)
@Composable
fun InverterSettingsViewPreview() {
    EnergyStatsTheme(darkTheme = true) {
        InverterSettingsView(
            FakeConfigManager(),
            NavHostController(LocalContext.current)
        )
    }
}