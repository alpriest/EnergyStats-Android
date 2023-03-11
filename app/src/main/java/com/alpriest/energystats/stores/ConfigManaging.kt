package com.alpriest.energystats.stores

import com.alpriest.energystats.models.Device
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

interface ConfigManaging {
    val themeStream: MutableStateFlow<AppTheme>
    var showBatteryTemperature: Boolean
    var useLargeDisplay: Boolean
    val minSOC: Double
    val batteryCapacity: Int
    var isDemoUser: Boolean
    var useColouredFlowLines: Boolean
    var refreshFrequency: RefreshFrequency
    var devices: List<Device>?
    var currentDevice: Device?
    var selectedDeviceID: String?
    fun logout()
    fun updateBatteryCapacity(capacity: String)
    suspend fun findDevices()
}
