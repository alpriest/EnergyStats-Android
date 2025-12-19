package com.alpriest.energystats.ui.statsgraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class NavigableStatsGraphTabViewModel : ViewModel() {
    val displayModeStream = MutableStateFlow<StatsDisplayMode>(StatsDisplayMode.Day(LocalDate.now()))

    fun updateCustomDateRange(start: LocalDate, end: LocalDate) {
        if (start > end) {
            return
        }

        val daysBetween = ChronoUnit.DAYS.between(start, end)
        val displayUnit = if (daysBetween > 31) CustomDateRangeDisplayUnit.MONTHS else CustomDateRangeDisplayUnit.DAYS

        val normalizedStart: LocalDate
        val normalizedEnd: LocalDate

        if (displayUnit == CustomDateRangeDisplayUnit.MONTHS) {
            normalizedStart = start.startOfMonth()
            normalizedEnd = end.endOfMonth()
        } else {
            normalizedStart = start
            normalizedEnd = end
        }

        displayModeStream.value = StatsDisplayMode.Custom(normalizedStart, normalizedEnd, displayUnit)
    }
}

class NavigableStatsGraphTabViewModelFactory(
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NavigableStatsGraphTabViewModel() as T
    }
}

fun LocalDate.startOfMonth(): LocalDate =
    withDayOfMonth(1)

fun LocalDate.endOfMonth(): LocalDate =
    withDayOfMonth(lengthOfMonth())
