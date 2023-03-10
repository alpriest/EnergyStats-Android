package com.alpriest.energystats.preview

import com.alpriest.energystats.models.*

class FakeConfigStore(
    override var isDemoUser: Boolean = true,
    override var useLargeDisplay: Boolean = false,
    override var useColouredFlowLines: Boolean = true,
    override var showBatteryTemperature: Boolean = true,
    override var refreshFrequency: Int = 0,
    override var selectedDeviceID: String? = null,
    override var devices: String? = null
) : ConfigInterface {
}