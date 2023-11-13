package com.alpriest.energystats.models

import com.alpriest.energystats.ui.settings.DisplayUnit
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

fun Double.asPercent(): String {
    return String.format("%.0f%%", (this * 100))
}

fun Int.Wh(decimalPlaces: Int): String {
    val divided = this.toDouble().rounded(decimalPlaces)
    val dec = DecimalFormat("#,###,###")
    return dec.format(divided) + " Wh"
}

fun Double.power(displayUnit: DisplayUnit, decimalPlaces: Int): String {
    return when (displayUnit) {
        DisplayUnit.Watts -> this.w()
        DisplayUnit.Kilowatts -> this.kW(decimalPlaces)
        DisplayUnit.Adaptive -> return if (this < 1) {
            this.w()
        } else {
            this.kW(decimalPlaces)
        }
    }
}

fun Double.energy(displayUnit: DisplayUnit, decimalPlaces: Int): String {
    return when (displayUnit) {
        DisplayUnit.Watts -> this.Wh(decimalPlaces)
        DisplayUnit.Kilowatts -> this.kWh(decimalPlaces)
        DisplayUnit.Adaptive -> return if (this < 1) {
            this.Wh(decimalPlaces)
        } else {
            this.kWh(decimalPlaces)
        }
    }
}

fun Double.Wh(decimalPlaces: Int): String {
    val divided = (this * 1000.0).rounded(decimalPlaces)
    val dec = DecimalFormat("#,###,###")
    return dec.format(divided) + " Wh"
}

fun Double.kWh(decimalPlaces: Int): String {
    val divided = this.rounded(decimalPlaces)
    val dec = DecimalFormat("#,###,##0." + "0".repeat(decimalPlaces))
    return dec.format(divided) + " kWh"
}

fun Double.kW(decimalPlaces: Int): String {
    val divided = this.rounded(decimalPlaces)
    val dec = DecimalFormat("#,###,##0." + "0".repeat(decimalPlaces))
    return dec.format(divided) + " kW"
}

fun Double.w(): String {
    val divided = (this * 1000.0).roundToLong()
    val dec = DecimalFormat("#,###")
    return dec.format(divided) + " W"
}

fun Double.rounded(decimalPlaces: Int): Double {
    val power = 10.0.pow(decimalPlaces.toDouble())
    return (this * power).roundToLong() / power
}

fun Double.sameValueAs(other: Double): Boolean {
    return abs(this - other) < 0.0000001
}