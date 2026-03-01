package com.alpriest.energystats.ui.statsgraph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.shared.config.ConfigManaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import java.util.Calendar

class StatsDatePickerHeaderViewModel(configManager: ConfigManaging, val displayModeStream: MutableStateFlow<StatsDisplayMode>) : ViewModel() {
    var rangeStream = MutableStateFlow<DatePickerRange>(DatePickerRange.DAY)
    var monthStream = MutableStateFlow(0)
    var yearStream = MutableStateFlow(0)
    var dateStream = MutableStateFlow<LocalDate>(LocalDate.now())
    var isInitialised = false
    var customStartDate = MutableStateFlow<LocalDate>(LocalDate.now().minusDays(30))
    val customStartDateString = MutableStateFlow("")
    var customEndDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val customEndDateString = MutableStateFlow("")
    var customRangeDisplayUnit: CustomDateRangeDisplayUnit = CustomDateRangeDisplayUnit.MONTHS
    var canDecreaseStream = MutableStateFlow(false)
    var canIncreaseStream = MutableStateFlow(false)
    var timeUsageGraphStyleStream = MutableStateFlow(configManager.statsTimeUsageGraphStyle)
    var energySourceUsageGraphShowingState = MutableStateFlow(configManager.showEnergySourceUsageGraphOnStats)

    init {
        monthStream.value = dateStream.value.monthValue
        yearStream.value = dateStream.value.year

        combine(rangeStream, dateStream, monthStream, yearStream) { _, _, _, _ -> Unit }
            .onEach { if (isInitialised) updateDisplayMode() }
            .launchIn(viewModelScope)

        combine(customStartDate, customEndDate) { _, _ -> Unit }
            .onEach { if (isInitialised) updateDisplayMode() }
            .launchIn(viewModelScope)

        displayModeStream
            .onEach { updateDatePickerRange(it) }
            .launchIn(viewModelScope)

        customStartDate
            .map { it.toString() }
            .onEach { customStartDateString.value = it }
            .launchIn(viewModelScope)

        customEndDate
            .map { it.toString() }
            .onEach { customEndDateString.value = it }
            .launchIn(viewModelScope)

        timeUsageGraphStyleStream
            .onEach { configManager.statsTimeUsageGraphStyle = it }
            .launchIn(viewModelScope)

        energySourceUsageGraphShowingState
            .onEach { configManager.showEnergySourceUsageGraphOnStats = it }
            .launchIn(viewModelScope)

        updateDatePickerRange(displayModeStream.value)

        updateIncreaseDecreaseButtons()
        isInitialised = true
    }

    private fun updateDatePickerRange(displayMode: StatsDisplayMode) {
        when (val displayMode = displayMode) {
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
                customRangeDisplayUnit = displayMode.unit
                rangeStream.value = DatePickerRange.CUSTOM(displayMode.start, displayMode.end)
            }
        }
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
                val currentMonth = calendar.get(Calendar.MONTH) + 1// Calendar.MONTH is zero-based
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
                if (monthStream.value - 1 < 1) {
                    monthStream.value = 12
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
                if (monthStream.value + 1 > 12) {
                    monthStream.value = 1
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