package com.alpriest.energystats.ui.flow.battery

import androidx.lifecycle.ViewModel
import com.alpriest.energystats.models.BatteryTemperatures
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.BatteryTemperatureDisplayMode

class BatteryPowerViewModel(
    private val configManager: ConfigManaging,
    private val actualStateOfCharge: Double,
    val chargePowerkWH: Double,
    private val batteryTemperatures: BatteryTemperatures,
    val residual: Int
) : ViewModel() {
    val temperatures: List<Double>
        get() {
            val result: List<Double> = when (configManager.batteryTemperatureDisplayMode) {
                BatteryTemperatureDisplayMode.Automatic -> listOf(batteryTemperatures.batTemperature, batteryTemperatures.batTemperature1, batteryTemperatures.batTemperature2).mapNotNull { it }
                BatteryTemperatureDisplayMode.Battery1 -> listOf(batteryTemperatures.batTemperature1).mapNotNull { it }
                BatteryTemperatureDisplayMode.Battery2 -> listOf(batteryTemperatures.batTemperature2).mapNotNull { it }
            }
            return result
        }

    private val calculator: BatteryCapacityCalculator = BatteryCapacityCalculator(
        capacityW = configManager.batteryCapacity.toDouble().toInt(),
        minimumSOC = configManager.minSOC
    )

    val batteryExtra: BatteryCapacityEstimate?
        get() {
            return calculator.batteryPercentageRemaining(
                batteryChargePowerkWH = chargePowerkWH,
                batteryStateOfCharge = actualStateOfCharge
            )
        }

    fun batteryStoredChargekWh(): Double {
        return calculator.currentEstimatedChargeAmountWh(batteryStateOfCharge = actualStateOfCharge, includeUnusableCapacity = !configManager.showUsableBatteryOnly) / 1000.0
    }

    fun batteryStateOfCharge(): Double {
        return calculator.effectiveBatteryStateOfCharge(batteryStateOfCharge = actualStateOfCharge, includeUnusableCapacity = !configManager.showUsableBatteryOnly)
    }

    fun setBatteryAsPercentage(value: Boolean) {
        configManager.showBatteryAsPercentage = value
    }

    val showUsableBatteryOnly: Boolean
        get() {
            return configManager.showUsableBatteryOnly
        }
}