package com.alpriest.energystats.ui.flow.battery

import com.alpriest.energystats.R
import com.alpriest.energystats.models.rounded
import java.lang.Math.abs
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
        if (kotlin.math.abs(batteryChargePowerkWH) <= 0) {
            return null
        }

        val currentEstimatedChargeW = capacityW * batteryStateOfCharge

        if (batteryChargePowerkWH > 0) { // battery charging
            if (batteryStateOfCharge >= percentageConsideredFull) {
                return null
            }

            val capacityRemainingW = capacityW - currentEstimatedChargeW
            val minsToFullCharge = (capacityRemainingW / (batteryChargePowerkWH * 1000.0)) * 60

            return BatteryCapacityEstimate(R.string.fullIn, minsToFullCharge.roundToInt())
        } else { // battery emptying
            if (batteryStateOfCharge <= (minimumSOC * 1.02)) {
                return null
            }
            val chargeRemaining = currentEstimatedChargeW - minimumCharge
            val minsUntilEmpty = (chargeRemaining / abs(batteryChargePowerkWH * 1000.0)) * 60

            return BatteryCapacityEstimate(R.string.emptyIn, minsUntilEmpty.roundToInt())
        }
    }

    fun currentEstimatedChargeAmountW(batteryStateOfCharge: Double, includeUnusableCapacity: Boolean = true): Double {
        return (capacityW * batteryStateOfCharge) - (if (includeUnusableCapacity) 0.0 else minimumCharge)
    }

    fun effectiveBatteryStateOfCharge(batteryStateOfCharge: Double, includeUnusableCapacity: Boolean = true): Double {
        if (batteryStateOfCharge > percentageConsideredFull) return 0.99

        val deduction = if (includeUnusableCapacity) 0.0 else minimumSOC
        return ((batteryStateOfCharge - deduction) / (1 - deduction)).rounded(decimalPlaces = 2)
    }
}
