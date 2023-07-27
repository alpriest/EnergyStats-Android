package com.alpriest.energystats.ui.settings.battery

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun BatterySOCSettingsView(config: ConfigManaging, network: Networking) {
}

@Preview(showBackground = true)
@Composable
fun BatterySOCSettingsViewPreview() {
    EnergyStatsTheme {
        BatterySOCSettingsView(
            config = FakeConfigManager(),
            network = DemoNetworking()
        )
    }
}