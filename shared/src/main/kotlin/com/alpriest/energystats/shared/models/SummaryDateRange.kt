package com.alpriest.energystats.shared.models

sealed class SummaryDateRange {
    object Automatic : SummaryDateRange()
    data class Manual(val from: MonthYear, val to: MonthYear) : SummaryDateRange()
}

data class MonthYear(
    var month: Int, // zero-based
    var year: Int
)
