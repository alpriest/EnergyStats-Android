package com.alpriest.energystats.ui.settings

import androidx.compose.runtime.Composable
import com.alpriest.energystats.models.PowerStationDetail
import com.alpriest.energystats.models.w
import com.alpriest.energystats.ui.settings.inverter.SettingsRow

@Composable
fun PowerStationSettingsView(powerStationDetail: PowerStationDetail) {
    SettingsPage {
        SettingsColumn {
            SettingsRow("Name", powerStationDetail.stationName)
            SettingsRow("Capacity", powerStationDetail.capacity.w())
            SettingsRow("Timezone", powerStationDetail.timezone)
        }
    }
}