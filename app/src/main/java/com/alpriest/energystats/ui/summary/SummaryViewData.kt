package com.alpriest.energystats.ui.summary

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
    )

    data class BestSolarData(
        val description: String,
        val amount: Double,
        val period: TimeGrouping,
    )
}

enum class TimeGrouping {
    MONTH,
    YEAR
}
