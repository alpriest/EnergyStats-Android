package com.alpriest.energystats.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.alpriest.energystats.models.w
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.inverter.SettingsRow

@Composable
fun PowerStationSettingsView(config: ConfigManaging) {
    trackScreenView("Power Station", "PowerStationSettingsView")

    LaunchedEffect(config.powerStationDetail) {
        config.fetchPowerStationDetail()
    }

    config.powerStationDetail?.let {
        SettingsPage {
            SettingsColumn {
                SettingsRow("Name", it.stationName)
                SettingsRow("Capacity", it.capacity.w())
                SettingsRow("Timezone", it.timezone)
            }
        }
    }
}