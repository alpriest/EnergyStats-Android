package com.alpriest.energystats.ui.statsgraph

import kotlin.math.pow
import kotlin.math.roundToInt

class AbsoluteSelfSufficiencyCalculator {
    fun calculate(grid: Double, feedIn: Double, loads: Double, batteryCharge: Double, batteryDischarge: Double): Double {
        val netGeneration = feedIn - grid + batteryDischarge - batteryCharge
        val homeConsumption = loads

        var result: Double = 0.0
        if (netGeneration > 0) {
            result = 1.0
        } else if (netGeneration + homeConsumption < 0) {
            result = 0.0
        } else if (netGeneration + homeConsumption > 0) {
            result = (netGeneration + homeConsumption) / homeConsumption
        }

        return (result * 100.0).roundTo(1)
    }
}

class NetSelfSufficiencyCalculator {
    fun calculate(loads: Double, grid: Double): Double {
        if (loads <= 0) {
            return 0.0
        }

        val result = 1 - (minOf(loads, maxOf(grid, 0.0)) / loads)

        return (result * 100.0).roundTo(1)
    }
}

fun Double.roundTo(decimalPlaces: Int): Double {
    val factor = 10.0.pow(decimalPlaces.toDouble())
    return (this * factor).roundToInt() / factor
}
