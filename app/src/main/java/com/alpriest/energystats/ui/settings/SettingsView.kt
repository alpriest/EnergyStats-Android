package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun RoundedColumnWithChild(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .background(colors.surface, shape = RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {
        content()
    }
}

@Composable
fun SettingsView(
    config: ConfigManaging,
    userManager: UserManaging,
    onLogout: () -> Unit,
    rawDataStore: RawDataStoring,
    onRateApp: () -> Unit,
    onSendUsEmail: () -> Unit,
    onBuyMeCoffee: () -> Unit
) {
    val scrollState = rememberScrollState()
    val currentDevice = config.currentDevice.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.padding(top = 6.dp))

        InverterChoiceView(config = config)

        FirmwareVersionView(config)

        currentDevice.value?.let {
            if (it.battery != null) {
                BatterySettingsView(
                    config = config
                )
            }
        }

        DisplaySettingsView(config)

        SelfSufficiencySettingsView(config, modifier = Modifier.fillMaxWidth())

        RefreshFrequencySettingsView(config)

        DataSettingsView(rawDataStore = rawDataStore)

        SettingsFooterView(config, userManager, onLogout, onRateApp, onSendUsEmail, onBuyMeCoffee)
    }
}

@Preview(showBackground = true, heightDp = 1200, widthDp = 300)
@Composable
fun SettingsViewPreview() {
    EnergyStatsTheme {
        SettingsView(config = FakeConfigManager(), userManager = FakeUserManager(), onLogout = {}, rawDataStore = RawDataStore(), onRateApp = {}, onSendUsEmail = {}, onBuyMeCoffee = {})
    }
}