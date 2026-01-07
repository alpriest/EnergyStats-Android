package com.alpriest.energystats.ui.statsgraph

import com.alpriest.energystats.models.CalculationBreakdown
import com.alpriest.energystats.shared.ui.roundedToString
import kotlin.math.pow
import kotlin.math.roundToInt

class NetSelfSufficiencyCalculator {
    fun calculate(grid: Double, feedIn: Double, loads: Double, batteryCharge: Double, batteryDischarge: Double): Pair<Double, CalculationBreakdown> {
        val netGeneration = feedIn - grid + batteryCharge - batteryDischarge
        val homeConsumption = loads
        val formula = """netGeneration = feedIn - grid + batteryCharge - batteryDischarge

If netGeneration > 0 then result = 1
Else if netGeneration + homeConsumption < 0 then result = 0
Else if netGeneration + homeConsumption > 0 then result = (netGeneration + homeConsumption) / homeConsumption
"""
        val calculation: (Int) -> String = { dp ->
            """netGeneration = ${feedIn.roundedToString(dp)} - ${grid.roundedToString(dp)} + ${batteryCharge.roundedToString(dp)} - ${batteryDischarge.roundedToString(dp)}

If ${netGeneration.roundedToString(dp)} > 0 then result = 1
Else if ${netGeneration.roundedToString(dp)} + ${homeConsumption.roundedToString(dp)} < 0 then result = 0
Else if ${netGeneration.roundedToString(dp)} + ${homeConsumption.roundedToString(dp)} > 0 then result = (${netGeneration.roundedToString(dp)} + ${homeConsumption.roundedToString(dp)}) / ${homeConsumption.roundedToString(dp)
            }
"""
        }

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
            CalculationBreakdown(formula, calculation)
        )
    }
}

class AbsoluteSelfSufficiencyCalculator {
    fun calculate(loads: Double, grid: Double): Pair<Double, CalculationBreakdown> {
        val formula = "1 - (min(loads, max(grid, 0.0)) / loads)"
        if (loads <= 0) {
            return Pair(0.0, CalculationBreakdown(formula, { "" }))
        }

        val result = 1 - (minOf(loads, maxOf(grid, 0.0)) / loads)

        return Pair(
            (result * 100.0).roundTo(1),
            CalculationBreakdown(formula, { "1 - (min(${loads.roundedToString(it)}, max(${grid.roundedToString(it)}, 0.0)) / ${loads.roundedToString(it)})" })
        )
    }
}

private fun Double.roundTo(decimalPlaces: Int): Double {
    val factor = 10.0.pow(decimalPlaces.toDouble())
    return (this * factor).roundToInt() / factor
}
