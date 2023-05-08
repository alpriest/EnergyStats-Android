package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.RawDataStore
import com.alpriest.energystats.models.RawDataStoring
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SettingsView(config: ConfigManaging, userManager: UserManaging, onLogout: () -> Unit, rawDataStore: RawDataStoring, onRateApp: () -> Unit, onSendUsEmail: () -> Unit) {
    val scrollState = rememberScrollState()
    val currentDevice = config.currentDevice.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        InverterChoiceView(config = config)

        FirmwareVersionView(config)

        currentDevice.value?.let {
            if (it.battery != null) {
                BatterySettingsView(
                    config = config
                )

                Divider()
            }
        }

        DisplaySettingsView(
            config = config
        )

        Divider()

        RefreshFrequencySettingsView(config)

        Divider()

        DataSettingsView(rawDataStore = rawDataStore)

        Divider()

        SettingsFooterView(config, userManager, onLogout, onRateApp, onSendUsEmail)
    }
}

@Preview(showBackground = true, heightDp = 600, widthDp = 300)
@Composable
fun SettingsViewPreview() {
    EnergyStatsTheme {
        SettingsView(config = FakeConfigManager(), userManager = FakeUserManager(), onLogout = {}, rawDataStore = RawDataStore(), onRateApp = {}, onSendUsEmail = {})
    }
}