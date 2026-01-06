package com.alpriest.energystats.shared.services

import com.alpriest.energystats.shared.R
import com.alpriest.energystats.shared.helpers.truncated
import kotlin.math.abs
import kotlin.math.roundToInt

data class BatteryCapacityEstimate(
    val stringId: Int,
    val duration: Int
)

class BatteryCapacityCalculator(
    private val capacityW: Int,
    private val minimumSOC: Double
) {
    private val minimumCharge: Double
        get() {
            return capacityW.toDouble() * minimumSOC
        }

    private val percentageConsideredFull = 98.75

    fun batteryPercentageRemaining(
        batteryChargePowerkWH: Double,
        batteryStateOfCharge: Double
    ): BatteryCapacityEstimate? {
        if (abs(batteryChargePowerkWH) <= 0) {
            return null
        }

        val currentEstimatedChargeW = capacityW * batteryStateOfCharge

        if (batteryChargePowerkWH > 0) { // battery charging
            val capacityRemainingW = capacityW - currentEstimatedChargeW
            val minsToFullCharge = (capacityRemainingW / (batteryChargePowerkWH * 1000.0)) * 60

            return BatteryCapacityEstimate(R.string.fullIn, minsToFullCharge.roundToInt())
        } else { // battery discharging
            val chargeRemaining = currentEstimatedChargeW - minimumCharge
            val minsUntilEmpty = (chargeRemaining / Math.abs(batteryChargePowerkWH * 1000.0)) * 60

            return BatteryCapacityEstimate(R.string.emptyIn, minsUntilEmpty.roundToInt())
        }
    }

    fun currentEstimatedChargeAmountWh(batteryStateOfCharge: Double, includeUnusableCapacity: Boolean = true): Double {
        val deduction = if (includeUnusableCapacity) 0.0 else minimumCharge
        return (capacityW * batteryStateOfCharge) - deduction
    }

    fun effectiveBatteryStateOfCharge(batteryStateOfCharge: Double, includeUnusableCapacity: Boolean = true): Double {
        if (batteryStateOfCharge > percentageConsideredFull) return 0.99

        val deduction = if (includeUnusableCapacity) 0.0 else minimumSOC
        return ((batteryStateOfCharge - deduction) / (1 - deduction)).truncated(decimalPlaces = 2)
    }
}