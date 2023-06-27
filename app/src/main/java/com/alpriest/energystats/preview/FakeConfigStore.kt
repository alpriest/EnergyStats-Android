package com.alpriest.energystats.preview

import com.alpriest.energystats.models.*

class FakeConfigStore(
    override var isDemoUser: Boolean = true,
    override var useLargeDisplay: Boolean = false,
    override var useColouredFlowLines: Boolean = true,
    override var showBatteryTemperature: Boolean = true,
    override var refreshFrequency: Int = 0,
    override var selectedDeviceID: String? = null,
    override var devices: String? = null,
    override var showSunnyBackground: Boolean = true,
    override var decimalPlaces: Int = 2,
    override var showBatteryEstimate: Boolean = true,
    override var showUsableBatteryOnly: Boolean = false,
    override var showTotalYield: Boolean = true,
    override var showSelfSufficiencyEstimate: Boolean = false
) : ConfigInterface {
}