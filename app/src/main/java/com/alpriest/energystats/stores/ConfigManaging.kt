package com.alpriest.energystats.stores

import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

interface ConfigManaging {
    val themeStream: MutableStateFlow<AppTheme>
    var showBatteryTemperature: Boolean
    var useLargeDisplay: Boolean
    val minSOC: Double
    val batteryCapacityW: Int
    val deviceSN: String?
    val deviceID: String?
    val hasPV: Boolean
    var hasBattery: Boolean
    var isDemoUser: Boolean
    var useColouredFlowLines: Boolean
    var refreshFrequency: RefreshFrequency
    fun logout()
    fun updateBatteryCapacity(capacity: String)
    suspend fun findDevice()
}
