package com.alpriest.energystats.ui.flow.battery

import androidx.lifecycle.ViewModel
import com.alpriest.energystats.stores.ConfigManaging

class BatteryPowerViewModel(
    private val configManager: ConfigManaging,
    private val actualStateOfCharge: Double,
    val chargePowerkWH: Double,
    val temperature: Double,
    val residual: Int
) : ViewModel() {
    private val calculator: BatteryCapacityCalculator

    init {
        calculator = BatteryCapacityCalculator(
            capacityW = configManager.batteryCapacity.toDouble().toInt(),
            minimumSOC = configManager.minSOC.value ?: 0.0
        )
    }

    val batteryExtra: BatteryCapacityEstimate?
        get() {
            return calculator.batteryPercentageRemaining(
                batteryChargePowerkWH = chargePowerkWH,
                batteryStateOfCharge = actualStateOfCharge
            )
        }

    fun batteryStoredCharge(): Double {
        return residual.toDouble() / 1000.0
    }

    fun batteryStateOfCharge(): Double {
        return calculator.effectiveBatteryStateOfCharge(batteryStateOfCharge = actualStateOfCharge, includeUnusableCapacity = !configManager.showUsableBatteryOnly)
    }
}