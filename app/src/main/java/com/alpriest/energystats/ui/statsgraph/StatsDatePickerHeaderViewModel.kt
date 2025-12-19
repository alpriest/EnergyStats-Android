package com.alpriest.energystats.ui.statsgraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar

class StatsDatePickerHeaderViewModel(val displayModeStream: MutableStateFlow<StatsDisplayMode>) : ViewModel() {
    var rangeStream = MutableStateFlow<DatePickerRange>(DatePickerRange.DAY)
    var monthStream = MutableStateFlow(0)
    var yearStream = MutableStateFlow(0)
    var dateStream = MutableStateFlow<LocalDate>(LocalDate.now())
    var isInitialised = false
    var customStartDate = MutableStateFlow<LocalDate>(LocalDate.now().minusDays(30))
    val customStartDateString = MutableStateFlow<String>("")
    var customEndDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val customEndDateString = MutableStateFlow<String>("")
    var customRangeDisplayUnit: CustomDateRangeDisplayUnit = CustomDateRangeDisplayUnit.DAYS
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

            updateIncreaseDecreaseButtons()
            isInitialised = true
        }

        viewModelScope.launch {
            customStartDate.collect {
                customStartDateString.value = it.toString()
            }
        }

        viewModelScope.launch {
            customEndDate.collect {
                customEndDateString.value = it.toString()
            }
        }
    }

    fun updateCustomDateRange(start: LocalDate, end: LocalDate) {
        if (start > end) {
            return
        }
        val daysBetween = ChronoUnit.DAYS.between(start, end)

        customStartDate.value = start
        customEndDate.value = end
        customRangeDisplayUnit = if (daysBetween > 31) CustomDateRangeDisplayUnit.DAYS else CustomDateRangeDisplayUnit.MONTHS
        rangeStream.value = DatePickerRange.CUSTOM(start, end)

        updateDisplayMode()
    }

    private fun makeUpdatedDisplayMode(range: DatePickerRange): StatsDisplayMode {
        return when (range) {
            is DatePickerRange.DAY -> StatsDisplayMode.Day(dateStream.value)
            is DatePickerRange.MONTH -> StatsDisplayMode.Month(monthStream.value, yearStream.value)
            is DatePickerRange.YEAR -> StatsDisplayMode.Year(yearStream.value)
            is DatePickerRange.CUSTOM -> StatsDisplayMode.Custom(range.start, range.end, customRangeDisplayUnit)
        }
    }

    private fun updateDisplayMode() {
        if (!isInitialised) {
            return
        }

        displayModeStream.value = makeUpdatedDisplayMode(rangeStream.value)
        updateIncreaseDecreaseButtons()
    }

    private fun updateIncreaseDecreaseButtons() {
        when (val displayMode = displayModeStream.value) {
            is StatsDisplayMode.Day -> {
                canIncreaseStream.value = displayMode.date.atStartOfDay() < LocalDate.now().atStartOfDay()
                canDecreaseStream.value = true
            }

            is StatsDisplayMode.Month -> {
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH) // Calendar.MONTH is zero-based
                val currentYear = calendar.get(Calendar.YEAR)
                canIncreaseStream.value = (displayMode.year < currentYear) || (displayMode.month < currentMonth && displayMode.year <= currentYear)
                canDecreaseStream.value = true
            }

            is StatsDisplayMode.Year -> {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                canIncreaseStream.value = displayMode.year < currentYear
                canDecreaseStream.value = true
            }

            is StatsDisplayMode.Custom -> {
                canIncreaseStream.value = false
                canDecreaseStream.value = false
            }
        }
    }

    fun decrease() {
        when (rangeStream.value) {
            is DatePickerRange.DAY -> {
                dateStream.value = dateStream.value.minusDays(1)
                canIncreaseStream.value = dateStream.value.atStartOfDay() < LocalDate.now().atStartOfDay()
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
                canIncreaseStream.value = dateStream.value.atStartOfDay() < LocalDate.now().atStartOfDay()
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