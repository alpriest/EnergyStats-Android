package com.alpriest.energystats.shared.helpers

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

fun Double.toCurrency(): String {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val currencySymbol = currencyFormat.currency?.symbol ?: ""
    return currencyFormat.format(this).replace(currencySymbol, "").trim()
}

val Float.celsius: String
    get() = this.roundToInt().toString() + "℃"

val Double.celsius: String
    get() = this.roundToInt().toString() + "℃"
