package com.alpriest.energystats.models

import java.text.DecimalFormat
import kotlin.math.abs

fun Double.asPercent(): String {
    return String.format("%.0f%%", (this * 100))
}

fun Int.kW(): String {
    val divided = this.toDouble().rounded(2)
    val dec = DecimalFormat("#,###,###")
    return dec.format(divided) + " kW"
}

fun Double.kW(): String {
    val divided = this.rounded(2)
    val dec = DecimalFormat("#,###,###.##")
    return dec.format(divided) + " kW"
}

fun Double.w(): String {
    val divided = Math.round(this * 1000.0)

    val dec = DecimalFormat("#,###,###.##")
    return dec.format(divided) + " kW"
}

fun Double.rounded(decimalPlaces: Int): Double {
    val power = Math.pow(10.0, decimalPlaces.toDouble())
    return Math.round(this * power) / power
}

fun Double.sameValueAs(other: Double): Boolean {
    return abs(this - other) < 0.0000001
}