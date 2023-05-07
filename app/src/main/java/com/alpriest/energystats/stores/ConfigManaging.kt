package com.alpriest.energystats.stores

import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.DeviceFirmwareVersion
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

interface ConfigManaging {
    val hasBattery: Boolean
    var showUsableBatteryOnly: Boolean
    var firmwareVersion: DeviceFirmwareVersion?
    val themeStream: MutableStateFlow<AppTheme>
    var decimalPlaces: Int
    var showSunnyBackground: Boolean
    var showBatteryEstimate: Boolean
    var showBatteryTemperature: Boolean
    var useLargeDisplay: Boolean
    val minSOC: Double
    val batteryCapacity: Int
    var isDemoUser: Boolean
    var useColouredFlowLines: Boolean
    var refreshFrequency: RefreshFrequency
    var devices: List<Device>?
    var currentDevice: MutableStateFlow<Device?>
    var selectedDeviceID: String?
    var variables: List<RawVariable>
    fun logout()
    fun updateBatteryCapacity(capacity: String)
    suspend fun fetchDevices()
    suspend fun fetchFirmwareVersions()
    suspend fun fetchVariables()
}
