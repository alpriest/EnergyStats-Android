package com.alpriest.energystats.shared.helpers

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

fun Double.toCurrency(decimalPlaces: Int = 2): String {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    currencyFormat.maximumFractionDigits = decimalPlaces
    val currencySymbol = currencyFormat.currency?.symbol ?: ""
    return currencyFormat.format(this).replace(currencySymbol, "").trim()
}

val Float.celsius: String
    get() = this.roundToInt().toString() + "℃"

val Double.celsius: String
    get() = this.roundToInt().toString() + "℃"
