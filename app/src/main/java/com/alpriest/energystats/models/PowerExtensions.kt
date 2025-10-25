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
    val divided = this.toDouble().truncated(decimalPlaces)
    val dec = DecimalFormat("#,###,###")
    return dec.format(divided) + " Wh"
}

fun Double.power(displayUnit: DisplayUnit, decimalPlaces: Int): String {
    return when (displayUnit) {
        DisplayUnit.Watts -> this.w()
        DisplayUnit.Kilowatts -> this.kW(decimalPlaces)
        DisplayUnit.Adaptive -> return if (abs(this) < 1) {
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
        DisplayUnit.Adaptive -> return if (abs(this) < 1) {
            this.Wh(decimalPlaces)
        } else {
            this.kWh(decimalPlaces)
        }
    }
}

fun Double.Wh(decimalPlaces: Int): String {
    val divided = (this * 1000.0).truncated(0)

    val numberFormatter = DecimalFormat().apply {
        minimumFractionDigits = decimalPlaces
        maximumFractionDigits = 0
        isGroupingUsed = false
    }

    return "${numberFormatter.format(divided)} Wh"
}

fun Double.kWh(decimalPlaces: Int, suffix: String = " kWh"): String {
    val divided = this.truncated(decimalPlaces)

    val numberFormatter = DecimalFormat().apply {
        minimumFractionDigits = decimalPlaces
        maximumFractionDigits = decimalPlaces
        isGroupingUsed = true
    }

    return "${numberFormatter.format(divided)}$suffix"
}

fun Double.kW(decimalPlaces: Int): String {
    val divided = this.truncated(decimalPlaces)

    val numberFormatter = DecimalFormat().apply {
        minimumFractionDigits = decimalPlaces
        maximumFractionDigits = decimalPlaces
        isGroupingUsed = false
    }

    return "${numberFormatter.format(divided)} kW"
}

fun Double.w(): String {
    val divided = (this * 1000.0).truncated(0)

    val numberFormatter = DecimalFormat().apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
        isGroupingUsed = false
    }

    return "${numberFormatter.format(divided)} W"
}

fun Double.truncated(decimalPlaces: Int): Double {
    val factor = 10.0.pow(decimalPlaces.toDouble())
    return (this * factor).roundToLong() / factor
}

fun Double.sameValueAs(other: Double): Boolean {
    return abs(this - other) < 0.0000001
}

fun Double.isFlowing(): Boolean {
    return !truncated(2).sameValueAs(0.0)
}