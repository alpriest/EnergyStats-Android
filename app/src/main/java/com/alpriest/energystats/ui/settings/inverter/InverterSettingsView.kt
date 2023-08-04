package com.alpriest.energystats.ui.settings.inverter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun InverterSettingsView(configManager: ConfigManaging) {
    val scrollState = rememberScrollState()
    val currentDevice = configManager.currentDevice.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .padding(12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        InverterChoiceView(configManager)

        currentDevice.value?.let {
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
        )
    }
}