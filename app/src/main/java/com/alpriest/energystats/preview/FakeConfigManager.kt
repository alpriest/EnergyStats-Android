package com.alpriest.energystats.preview

import com.alpriest.energystats.models.Battery
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.DeviceFirmwareVersion
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
            showBatteryTemperature = showBatteryTemperature,
            decimalPlaces = 2,
            showSunnyBackground = true,
            showBatteryEstimate = true
        )
    )
    override var decimalPlaces: Int
        get() = 2
        set(value) {}
    override var showSunnyBackground: Boolean
        get() = true
        set(value) {}
    override var showBatteryEstimate: Boolean
        get() = true
        set(value) {}
    override var showBatteryTemperature: Boolean
        get() = true
        set(value) {}
    override var useLargeDisplay: Boolean = false
    override val minSOC: Double
        get() = 0.2
    override val batteryCapacity: Int
        get() = 10000
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
        get() = listOf(
            Device(plantName = "plant1", deviceID = "abcdef", deviceSN = "123123", battery = Battery("5200", "20"), hasPV = true),
            Device(plantName = "plant2", deviceID = "ppplll", deviceSN = "998877", battery = Battery("5200", "20"), hasPV = true)
        )
        set(value) {}
    override var currentDevice: Device?
        get() = devices?.first()
        set(value) {}
    override var selectedDeviceID: String?
        get() = "abcdef"
        set(value) {}

    override fun logout() {
    }

    override fun updateBatteryCapacity(capacity: String) {
    }

    override suspend fun findDevices() {
    }

    override suspend fun fetchFirmwareVersions() {
        firmwareVersion = DeviceFirmwareVersion("1.50", "1.09", "1.49")
    }

    override var firmwareVersion: DeviceFirmwareVersion? = null
}
