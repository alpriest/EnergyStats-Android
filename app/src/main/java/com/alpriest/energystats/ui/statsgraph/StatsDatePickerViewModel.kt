package com.alpriest.energystats.ui.statsgraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

class StatsDatePickerViewModel(val displayModeStream: MutableStateFlow<StatsDisplayMode>) : ViewModel() {
    var rangeStream = MutableStateFlow<DatePickerRange>(DatePickerRange.DAY)
    var monthStream = MutableStateFlow(0)
    var yearStream = MutableStateFlow(0)
    var dateStream = MutableStateFlow<LocalDate>(LocalDate.now())
    var isInitialised = false
    var customStartDate = MutableStateFlow<LocalDate>(LocalDate.now().minusDays(30))
    var customEndDate = MutableStateFlow<LocalDate>(LocalDate.now())
    var canDecreaseStream = MutableStateFlow(false)
    var canIncreaseStream = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            monthStream.value = dateStream.value.monthValue - 1
            yearStream.value = dateStream.value.year

            combine(rangeStream, dateStream, monthStream, yearStream) { _, _, _, _ ->
                updateDisplayMode()
            }.collect { }
        }
        viewModelScope.launch {
            combine(customStartDate, customEndDate) { _, _ ->
                updateDisplayMode()
            }.collect { }
        }

        viewModelScope.launch {
            when (val displayMode = displayModeStream.value) {
                is StatsDisplayMode.Day -> {
                    dateStream.value = displayMode.date
                    rangeStream.value = DatePickerRange.DAY
                    canIncreaseStream.value = displayMode.date.atStartOfDay() < LocalDate.now().atStartOfDay()
                    canDecreaseStream.value = true
                }

                is StatsDisplayMode.Month -> {
                    monthStream.value = displayMode.month
                    yearStream.value = displayMode.year
                    rangeStream.value = DatePickerRange.MONTH

                    val calendar = Calendar.getInstance()
                    val currentMonth = calendar.get(Calendar.MONTH) // Calendar.MONTH is zero-based
                    val currentYear = calendar.get(Calendar.YEAR)
                    canIncreaseStream.value = (displayMode.year < currentYear) || (displayMode.month < currentMonth && displayMode.year <= currentYear)
                    canDecreaseStream.value = true
                }

                is StatsDisplayMode.Year -> {
                    yearStream.value = displayMode.year
                    rangeStream.value = DatePickerRange.YEAR

                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    canIncreaseStream.value = displayMode.year < currentYear
                    canDecreaseStream.value = true
                }

                is StatsDisplayMode.Custom -> {
                    customStartDate.value = displayMode.start
                    customEndDate.value = displayMode.end
                    rangeStream.value = DatePickerRange.CUSTOM(displayMode.start, displayMode.end)
                    canIncreaseStream.value = false
                    canDecreaseStream.value = true
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
            is DatePickerRange.CUSTOM -> StatsDisplayMode.Custom(customStartDate.value, customEndDate.value)
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
            is DatePickerRange.DAY -> {
                dateStream.value = dateStream.value.plusDays(1)
            }

            is DatePickerRange.MONTH -> {
                if (monthStream.value + 1 > 11) {
                    monthStream.value = 0
                    yearStream.value += 1
                } else {
                    monthStream.value += 1
                }
            }

            is DatePickerRange.YEAR -> yearStream.value += 1
            is DatePickerRange.CUSTOM -> {}
        }

        updateDisplayMode()
    }
}