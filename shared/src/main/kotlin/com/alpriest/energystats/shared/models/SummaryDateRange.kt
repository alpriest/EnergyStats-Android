package com.alpriest.energystats.shared.models

import java.time.LocalDate

sealed class SummaryDateRange {
    object Automatic : SummaryDateRange()
    data class Manual(val from: LocalDate, val to: LocalDate) : SummaryDateRange()
}
