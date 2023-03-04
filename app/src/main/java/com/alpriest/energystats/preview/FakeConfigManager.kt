package com.alpriest.energystats.preview

import com.alpriest.energystats.stores.ConfigManaging
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
    override val batteryCapacityW: Int
        get() = 10000
    override val deviceSN: String
        get() = "1234"
    override val deviceID: String
        get() = "4567"
    override val hasPV: Boolean
        get() = true
    override var hasBattery: Boolean
        get() = true
        set(value) {}
    override var isDemoUser: Boolean
        get() = true
        set(value) {}
    override var useColouredFlowLines: Boolean
        get() = true
        set(value) {}

    override fun logout() {
    }

    override fun updateBatteryCapacity(capacity: String) {
    }

    override suspend fun findDevice() {
    }
}
