package com.alpriest.energystats.ui.flow.battery

import androidx.lifecycle.ViewModel
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.models.kW

class BatteryPowerViewModel(
    val configManager: ConfigManaging,
    val batteryStateOfCharge: Double,
    val batteryChargePowerkWH: Double
) : ViewModel() {
    private val calculator: BatteryCapacityCalculator

    init {
        calculator = BatteryCapacityCalculator(
            capacityW = configManager.batteryCapacityW,
            configManager.minSOC
        )
    }

    val batteryExtra: String?
        get() {
            return calculator.batteryPercentageRemaining(
                batteryChargePowerkWH = batteryChargePowerkWH,
                batteryStateOfCharge = batteryStateOfCharge
            )
        }

    val batteryCapacity: String
        get() {
            return calculator.currentEstimatedChargeAmountkWH(batteryStateOfCharge = batteryStateOfCharge)
                .kW()
        }
}