package com.alpriest.energystats.preview

import com.alpriest.energystats.stores.ConfigManaging

@Suppress("UNUSED_PARAMETER")
class FakeConfigManager : ConfigManaging {
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

    override fun logout() {
    }

    override suspend fun findDevice() {
    }
}
