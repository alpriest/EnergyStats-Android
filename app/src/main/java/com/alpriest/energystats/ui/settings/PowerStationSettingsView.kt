package com.alpriest.energystats.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alpriest.energystats.models.PowerStationDetail
import com.alpriest.energystats.models.w
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.ui.settings.inverter.SettingsRow

@Composable
fun PowerStationSettingsView(powerStationDetail: PowerStationDetail, modifier: Modifier) {
    trackScreenView("Power Station", "PowerStationSettingsView")

    SettingsPage(modifier) {
        SettingsColumn {
            SettingsRow("Name", powerStationDetail.stationName)
            SettingsRow("Capacity", powerStationDetail.capacity.w())
            SettingsRow("Timezone", powerStationDetail.timezone)
        }
    }
}