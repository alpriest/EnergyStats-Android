package com.alpriest.energystats.ui.flow.battery

import android.icu.text.RelativeDateTimeFormatter
import java.lang.Math.abs
import kotlin.math.roundToInt

class BatteryCapacityCalculator(
    private val capacityW: Int,
    private val minimumSOC: Double,
    private val formatter: RelativeDateTimeFormatter = RelativeDateTimeFormatter.getInstance()
) {
    private val minimumCharge: Double
        get() {
            return capacityW.toDouble() * minimumSOC
        }

    fun batteryPercentageRemaining(
        batteryChargePowerkWH: Double,
        batteryStateOfCharge: Double
    ): String? {
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
            val duration = duration(minsToFullCharge.roundToInt())

            return "Full in $duration"
        } else { // battery emptying
            if (batteryStateOfCharge <= (minimumSOC * 1.02)) {
                return null
            }
            val chargeRemaining = currentEstimatedChargeW - minimumCharge
            val minsUntilEmpty = (chargeRemaining / abs(batteryChargePowerkWH * 1000.0)) * 60
            val duration = duration(minsUntilEmpty.roundToInt())

            return "Empty in $duration"
        }
    }

    fun currentEstimatedChargeAmountkWH(batteryStateOfCharge: Double): Double {
        return (capacityW * batteryStateOfCharge) / 1000.0
    }

    private fun duration(minutes: Int): String {
        return when (minutes) {
            in 0..60 -> "$minutes mins"
            in 61..119 -> "${minutes / 60} hour"
            else -> "${Math.round(minutes / 60.0)} hours"
        }
    }
}
