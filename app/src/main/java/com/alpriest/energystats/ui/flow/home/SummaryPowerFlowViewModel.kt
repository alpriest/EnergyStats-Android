package com.alpriest.energystats.ui.flow.home

import androidx.lifecycle.ViewModel
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.battery.BatteryPowerViewModel

class SummaryPowerFlowViewModel(
    val configManager: ConfigManaging,
    val solar: Double,
    val battery: Double,
    val home: Double,
    val grid: Double,
    val batteryStateOfCharge: Double,
    val hasBattery: Boolean
) : ViewModel() {
    val batteryViewModel: BatteryPowerViewModel =
        BatteryPowerViewModel(configManager, batteryStateOfCharge, battery)
}