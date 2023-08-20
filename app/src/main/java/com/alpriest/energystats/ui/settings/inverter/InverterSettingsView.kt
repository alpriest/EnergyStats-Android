package com.alpriest.energystats.ui.settings.inverter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun InverterSettingsView(configManager: ConfigManaging, navController: NavHostController) {
    val currentDevice = configManager.currentDevice.collectAsState()
    val showInverterTemperaturesState = rememberSaveable { mutableStateOf(configManager.showInverterTemperatures) }

    SettingsPage {
        InverterChoiceView(configManager)

        currentDevice.value?.let {
            SettingsNavButton(stringResource(R.string.configure_work_mode)) { navController.navigate(SettingsScreen.InverterWorkMode.name) }

            SettingsColumnWithChild {
                SettingsCheckbox(
                    title = "Show inverter temperatures",
                    state = showInverterTemperaturesState,
                    onConfigUpdate = { configManager.showInverterTemperatures = it }
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
        SettingsRow("Has Battery", if (device.battery != null) "true" else "false")
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