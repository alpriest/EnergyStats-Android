package com.alpriest.energystats.ui.statsgraph

import kotlin.math.pow
import kotlin.math.roundToInt

class SelfSufficiencyCalculator {
    fun calculate(generation: Double, feedIn: Double, grid: Double, batteryCharge: Double, batteryDischarge: Double): Double {
        val homeConsumption = generation - feedIn + grid + batteryDischarge - batteryCharge
        val selfServedPower = generation + batteryDischarge

        val result = maxOf(0.0, minOf(1.0, selfServedPower / homeConsumption)) - grid / homeConsumption

        return result.roundTo(3) * 100.0
    }

    private fun Double.roundTo(decimalPlaces: Int): Double {
        val factor = 10.0.pow(decimalPlaces.toDouble())
        return (this * factor).roundToInt() / factor
    }
}
