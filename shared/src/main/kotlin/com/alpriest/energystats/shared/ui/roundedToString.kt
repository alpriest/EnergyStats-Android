package com.alpriest.energystats.shared.ui

import com.alpriest.energystats.shared.helpers.truncated
import java.text.NumberFormat
import java.util.Locale

fun Double.roundedToString(decimalPlaces: Int, currencySymbol: String = "", locale: Locale = Locale.getDefault()): String {
    val roundedNumber = this.truncated(decimalPlaces)

    val numberFormat = NumberFormat.getNumberInstance(locale)
    numberFormat.maximumFractionDigits = decimalPlaces
    numberFormat.minimumFractionDigits = decimalPlaces

    val formattedNumber = numberFormat.format(roundedNumber)

    return "$currencySymbol$formattedNumber"
}