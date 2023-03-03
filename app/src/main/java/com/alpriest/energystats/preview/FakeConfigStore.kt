package com.alpriest.energystats.preview

import com.alpriest.energystats.models.*

class FakeConfigStore(
    override var minSOC: String? = "20",
    override var batteryCapacityW: String? = "5400",
    override var deviceID: String? = "1234567890",
    override var deviceSN: String? = "ABCDEFGHIJK",
    override var hasBattery: Boolean = true,
    override var hasPV: Boolean = true,
    override var isDemoUser: Boolean = true,
    override var useLargeDisplay: Boolean = false,
    override var useColouredFlowLines: Boolean = true
) : ConfigInterface {

}