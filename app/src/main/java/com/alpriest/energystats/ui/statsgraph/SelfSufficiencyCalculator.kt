package com.alpriest.energystats.ui.statsgraph

import com.alpriest.energystats.ui.CalculationBreakdown
import kotlin.math.pow
import kotlin.math.roundToInt

class AbsoluteSelfSufficiencyCalculator {
    fun calculate(grid: Double, feedIn: Double, loads: Double, batteryCharge: Double, batteryDischarge: Double): Pair<Double, CalculationBreakdown> {
        val netGeneration = feedIn - grid + batteryDischarge - batteryCharge
        val homeConsumption = loads
        var formula = "netGeneration = (feedIn - grid + batteryDischarge - batteryCharge)\n" +
                "if (netGeneration > 0) {\n" +
                "    result = 1.0\n" +
                "} else if (netGeneration + homeConsumption < 0) {\n" +
                "    result = 0.0\n" +
                "} else if (netGeneration + homeConsumption > 0) {\n" +
                "    result = (netGeneration + homeConsumption) / homeConsumption\n" +
                "}"

        var result = 0.0
        if (netGeneration > 0) {
            result = 1.0
        } else if (netGeneration + homeConsumption < 0) {
            result = 0.0
        } else if (netGeneration + homeConsumption > 0) {
            result = (netGeneration + homeConsumption) / homeConsumption
        }

        return Pair(
            (result * 100.0).roundTo(1),
            CalculationBreakdown(formula,"TOOD")
        )
    }
}

class NetSelfSufficiencyCalculator {
    fun calculate(loads: Double, grid: Double): Pair<Double, CalculationBreakdown> {
        val formula = "1 - (min(loads, max(grid, 0.0)) / loads)"
        if (loads <= 0) {
            return Pair(0.0, CalculationBreakdown(formula,""))
        }

        val result = 1 - (minOf(loads, maxOf(grid, 0.0)) / loads)

        return Pair(
            (result * 100.0).roundTo(1),
            CalculationBreakdown(formula,"1 - (min($loads, max($grid, 0.0)) / $loads)")
        )
    }
}

fun Double.roundTo(decimalPlaces: Int): Double {
    val factor = 10.0.pow(decimalPlaces.toDouble())
    return (this * factor).roundToInt() / factor
}
