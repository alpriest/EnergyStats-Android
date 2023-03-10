package com.alpriest.energystats.preview

import com.alpriest.energystats.models.Device
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("UNUSED_PARAMETER")
class FakeConfigManager : ConfigManaging {
    override val themeStream: MutableStateFlow<AppTheme> = MutableStateFlow(
        AppTheme(
            useLargeDisplay = true,
            useColouredLines = true,
            showBatteryTemperature = showBatteryTemperature
        )
    )
    override var showBatteryTemperature: Boolean
        get() = true
        set(value) {}
    override var useLargeDisplay: Boolean = false
    override val minSOC: Double
        get() = 0.2
    override val batteryCapacity: String
        get() = "10000"
    override var isDemoUser: Boolean
        get() = true
        set(value) {}
    override var useColouredFlowLines: Boolean
        get() = true
        set(value) {}
    override var refreshFrequency: RefreshFrequency
        get() = RefreshFrequency.Auto
        set(value) {}
    override var devices: List<Device>?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var currentDevice: Device?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var selectedDeviceID: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun logout() {
    }

    override fun updateBatteryCapacity(capacity: String) {
    }

    override suspend fun findDevices() {
    }
}
