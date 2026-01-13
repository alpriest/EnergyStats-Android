package com.alpriest.energystats.ui.flow.earnings

import android.content.Context
import com.alpriest.energystats.shared.ui.roundedToString

class FinanceAmount(val shortTitleResId: Int, val amount: Double) {
    fun formattedAmount(currencySymbol: String): String {
        return amount.roundedToString(2, currencySymbol)
    }

    fun title(context: Context): String {
        return context.getString(shortTitleResId)
    }
}