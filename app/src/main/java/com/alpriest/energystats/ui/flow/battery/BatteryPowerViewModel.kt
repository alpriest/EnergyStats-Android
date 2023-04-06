package com.alpriest.energystats.ui.flow.battery

import androidx.lifecycle.ViewModel
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.models.kW

class BatteryPowerViewModel(
    private val configManager: ConfigManaging,
    private val actualBatteryStateOfCharge: Double,
    val batteryChargePowerkWH: Double,
    val batteryTemperature: Double
) : ViewModel() {
    private val calculator: BatteryCapacityCalculator

    init {
        calculator = BatteryCapacityCalculator(
            capacityW = configManager.batteryCapacity.toDouble().toInt(),
            configManager.minSOC
        )
    }

    val batteryExtra: BatteryCapacityEstimate?
        get() {
            return calculator.batteryPercentageRemaining(
                batteryChargePowerkWH = batteryChargePowerkWH,
                batteryStateOfCharge = actualBatteryStateOfCharge
            )
        }

    fun batteryStoredChargekW(decimalPlaces: Int): String {
        return (calculator.currentEstimatedChargeAmountW(batteryStateOfCharge = actualBatteryStateOfCharge, includeUnusableCapacity = !configManager.showUsableBatteryOnly) / 1000.0).kW(decimalPlaces = decimalPlaces)
    }

    fun batteryStateOfCharge(): Double {
        return calculator.effectiveBatteryStateOfCharge(batteryStateOfCharge = actualBatteryStateOfCharge, includeUnusableCapacity = !configManager.showUsableBatteryOnly)
    }
}