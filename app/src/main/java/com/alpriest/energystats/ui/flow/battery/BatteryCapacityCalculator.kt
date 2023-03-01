package com.alpriest.energystats.ui.flow.battery

import com.alpriest.energystats.R
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

    fun batteryPercentageRemaining(
        batteryChargePowerkWH: Double,
        batteryStateOfCharge: Double
    ): BatteryCapacityEstimate? {
        if (kotlin.math.abs(batteryChargePowerkWH) <= 0) {
            return null
        }

        val currentEstimatedChargeW = capacityW * batteryStateOfCharge

        if (batteryChargePowerkWH > 0) { // battery charging
            if (batteryStateOfCharge >= 98.99) {
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

    fun currentEstimatedChargeAmountkWH(batteryStateOfCharge: Double): Double {
        return (capacityW * batteryStateOfCharge) / 1000.0
    }
}
