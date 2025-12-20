package com.alpriest.energystats.ui.statsgraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

class NavigableStatsGraphTabViewModel : ViewModel() {
    val displayModeStream = MutableStateFlow<StatsDisplayMode>(StatsDisplayMode.Day(LocalDate.now()))

    fun updateCustomDateRange(start: LocalDate, end: LocalDate, unit: CustomDateRangeDisplayUnit) {
        if (start > end) {
            return
        }

        displayModeStream.value = StatsDisplayMode.Custom(start, end, unit)
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
