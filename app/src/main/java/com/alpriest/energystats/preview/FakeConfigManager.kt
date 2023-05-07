package com.alpriest.energystats.preview

import com.alpriest.energystats.models.Battery
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.DeviceFirmwareVersion
import com.alpriest.energystats.models.RawVariable
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
            showBatteryTemperature = true,
            decimalPlaces = 2,
            showSunnyBackground = true,
            showBatteryEstimate = true,
            showUsableBatteryOnly = false
        )
    )
    override var decimalPlaces: Int = 2
    override var showSunnyBackground: Boolean = true
    override var showBatteryEstimate: Boolean = true
    override var showBatteryTemperature: Boolean = true
    override var useLargeDisplay: Boolean = false
    override val minSOC: Double = 0.2
    override val batteryCapacity: Int = 10000
    override var isDemoUser: Boolean = true
    override var useColouredFlowLines: Boolean = true
    override var refreshFrequency: RefreshFrequency = RefreshFrequency.Auto
    override var devices: List<Device>?
        get() = listOf(
            Device(plantName = "plant1", deviceID = "abcdef", deviceSN = "123123", battery = Battery("5200", "20"), hasPV = true, deviceType = "F3000"),
            Device(plantName = "plant2", deviceID = "ppplll", deviceSN = "998877", battery = Battery("5200", "20"), hasPV = true, deviceType = "H1-3.7-E")
        )
        set(value) {}
    override var currentDevice: MutableStateFlow<Device?> = MutableStateFlow(null)
    override var selectedDeviceID: String? = "abcdef"
    override var variables: List<RawVariable> = listOf()
    override val hasBattery: Boolean = true

    override fun logout() {
    }

    override fun updateBatteryCapacity(capacity: String) {
    }

    override suspend fun fetchDevices() {
    }

    override suspend fun fetchFirmwareVersions() {
        firmwareVersion = DeviceFirmwareVersion("1.50", "1.09", "1.49")
    }

    override suspend fun fetchVariables() {
    }

    override var showUsableBatteryOnly: Boolean = false
    override var firmwareVersion: DeviceFirmwareVersion? = null
}
