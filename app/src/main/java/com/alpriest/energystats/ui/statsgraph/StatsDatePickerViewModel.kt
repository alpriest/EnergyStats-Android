package com.alpriest.energystats.ui.statsgraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

class StatsDatePickerViewModel(val displayModeStream: MutableStateFlow<StatsDisplayMode>) : ViewModel() {
    var rangeStream = MutableStateFlow<DatePickerRange>(DatePickerRange.DAY)
    var monthStream = MutableStateFlow(0)
    var yearStream = MutableStateFlow(0)
    var dateStream = MutableStateFlow<LocalDate>(LocalDate.now())
    var isInitialised = false
    var customStartDate = MutableStateFlow<LocalDate>(LocalDate.now())
    var customEndDate = MutableStateFlow<LocalDate>(LocalDate.now())

    init {
        viewModelScope.launch {
            monthStream.value = dateStream.value.monthValue - 1
            yearStream.value = dateStream.value.year

            combine(rangeStream, dateStream, monthStream, yearStream) { _, _, _, _ ->
                updateDisplayMode()
            }.collect { }
        }

        viewModelScope.launch {
            when (val displayMode = displayModeStream.value) {
                is StatsDisplayMode.Day -> {
                    dateStream.value = displayMode.date
                    rangeStream.value = DatePickerRange.DAY
                }
                is StatsDisplayMode.Month -> {
                    monthStream.value = displayMode.month
                    yearStream.value = displayMode.year
                    rangeStream.value = DatePickerRange.MONTH
                }
                is StatsDisplayMode.Year -> {
                    yearStream.value = displayMode.year
                    rangeStream.value = DatePickerRange.YEAR
                }
                is StatsDisplayMode.Custom -> {
                    customStartDate.value = displayMode.start
                    customEndDate.value = displayMode.end
                    rangeStream.value = DatePickerRange.CUSTOM(displayMode.start, displayMode.end)
                }
            }

            isInitialised = true
        }
    }

    private fun makeUpdatedDisplayMode(range: DatePickerRange): StatsDisplayMode {
        return when (range) {
            is DatePickerRange.DAY -> StatsDisplayMode.Day(dateStream.value)
            is DatePickerRange.MONTH -> StatsDisplayMode.Month(monthStream.value, yearStream.value)
            is DatePickerRange.YEAR -> StatsDisplayMode.Year(yearStream.value)
            is DatePickerRange.CUSTOM -> StatsDisplayMode.Custom(range.start, range.end)
        }
    }

    private fun updateDisplayMode() {
        if (!isInitialised) {
            return
        }

        displayModeStream.value = makeUpdatedDisplayMode(rangeStream.value)
    }

    fun decrease() {
        when (rangeStream.value) {
            is DatePickerRange.DAY -> {
                dateStream.value = dateStream.value.minusDays(1)
            }
            is DatePickerRange.MONTH -> {
                if (monthStream.value - 1 < 0) {
                    monthStream.value = 11
                    yearStream.value -= 1
                } else {
                    monthStream.value -= 1
                }
            }
            is DatePickerRange.YEAR -> yearStream.value -= 1
            is DatePickerRange.CUSTOM -> {}
        }

        updateDisplayMode()
    }

    fun increase() {
        when (rangeStream.value) {
            DatePickerRange.DAY -> {
                dateStream.value = dateStream.value.plusDays(1)
            }
            DatePickerRange.MONTH -> {
                if (monthStream.value + 1 > 11) {
                    monthStream.value = 0
                    yearStream.value += 1
                } else {
                    monthStream.value += 1
                }
            }
            DatePickerRange.YEAR -> yearStream.value += 1
            is DatePickerRange.CUSTOM -> {}
        }

        updateDisplayMode()
    }
}