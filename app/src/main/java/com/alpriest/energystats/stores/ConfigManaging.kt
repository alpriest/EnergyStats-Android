package com.alpriest.energystats.stores

import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.ui.settings.SelfSufficiencyEstimateMode
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

interface ConfigManaging {
    val variables: List<RawVariable>
    val hasBattery: Boolean
    var showUsableBatteryOnly: Boolean
    val themeStream: MutableStateFlow<AppTheme>
    var decimalPlaces: Int
    var showTotalYield: Boolean
    var showSunnyBackground: Boolean
    var selfSufficiencyEstimateMode: SelfSufficiencyEstimateMode
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
    val selectedDeviceID: String?
    fun logout()
    fun updateBatteryCapacity(capacity: String)
    suspend fun fetchDevices()
    suspend fun refreshFirmwareVersion()
    fun select(device: Device)
    var appVersion: String
}
