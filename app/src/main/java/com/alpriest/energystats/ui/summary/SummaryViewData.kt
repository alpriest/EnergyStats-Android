package com.alpriest.energystats.ui.summary

import android.content.Context
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.helpers.monthYearString
import java.time.LocalDate

data class SummaryViewData(
    val solar: Double?,
    val homeUsage: Double?,
    val financialData: FinancialData?,
    val bestSolar: BestSolarData?,
    val hasPV: Boolean,
    val oldestDataDate: String,
    val latestDataDate: String,
    val currencySymbol: String,
) {

    data class FinancialData(
        val exportIncome: Double,
        val gridImportAvoided: Double,
        val totalBenefit: Double,
        val payback: PaybackData?
    )

    data class BestSolarData(
        val description: String,
        val amount: Double,
        val period: TimeGrouping,
    )

    data class PaybackData(
        val paybackMonths: Int,
        val installationPurchasePrice: String,
        private val oldestDataDate: LocalDate
    ) {
        fun text(infoTextFormatString: String) {
            val monthYear = oldestDataDate.monthYearString()

            String.format(
                infoTextFormatString,
                monthYear,
                installationPurchasePrice
            )
        }

        companion object {
            fun create(
                paybackMonths: Int?,
                purchasePrice: String?,
                oldestDataDate: LocalDate,
            ): PaybackData? {
                if (paybackMonths == null || purchasePrice == null) return null

                return PaybackData(
                    paybackMonths = paybackMonths,
                    installationPurchasePrice = purchasePrice,
                    oldestDataDate = oldestDataDate
                )
            }
        }
    }
}

enum class TimeGrouping {
    MONTH,
    YEAR;

    fun title(context: Context): String {
        return when (this) {
            MONTH -> context.getString(R.string.month).lowercase()
            YEAR -> context.getString(R.string.year).lowercase()
        }
    }
}
