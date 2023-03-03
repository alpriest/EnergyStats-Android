package com.alpriest.energystats.stores

import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

interface ConfigManaging {
    val themeStream: MutableStateFlow<AppTheme>
    var useLargeDisplay: Boolean
    val minSOC: Double
    val batteryCapacityW: Int
    val deviceSN: String?
    val deviceID: String?
    val hasPV: Boolean
    var hasBattery: Boolean
    var isDemoUser: Boolean
    var useColouredFlowLines: Boolean
    fun logout()
    fun updateBatteryCapacity(capacity: String)
    suspend fun findDevice()
}
