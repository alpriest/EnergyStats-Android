package com.alpriest.energystats.ui.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.*

class StatsDatePickerViewModel(val displayModeStream: MutableStateFlow<StatsDisplayMode>) : ViewModel() {
    var rangeStream = MutableStateFlow(DatePickerRange.DAY)
    var monthStream = MutableStateFlow(0)
    var yearStream = MutableStateFlow(0)
    var dateStream = MutableStateFlow<Long>(Calendar.getInstance().timeInMillis)
    var isInitialised = false

    init {
        viewModelScope.launch {
            val displayMode = displayModeStream.value
            when (displayMode) {
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
            }

            isInitialised = true

            rangeStream.collect { newValue ->
                updateDisplayMode()
            }
        }
    }

    private fun makeUpdatedDisplayMode(range: DatePickerRange): StatsDisplayMode {
        return when (range) {
            DatePickerRange.DAY -> StatsDisplayMode.Day(dateStream.value)
            DatePickerRange.MONTH -> StatsDisplayMode.Month(monthStream.value, yearStream.value)
            DatePickerRange.YEAR -> StatsDisplayMode.Year(yearStream.value)
        }
    }

    private fun updateDisplayMode() {
        if (!isInitialised) { return }

        displayModeStream.value = makeUpdatedDisplayMode(rangeStream.value)
    }

    fun decrease() {
        when (rangeStream.value) {
            DatePickerRange.DAY -> dateStream.value = dateStream.value - 86400
            DatePickerRange.MONTH -> {
                if (monthStream.value - 1 < 0) {
                    monthStream.value = 11
                    yearStream.value -= 1
                } else {
                    monthStream.value -= 1
                }
            }
            DatePickerRange.YEAR -> yearStream.value -= 1
        }

        updateDisplayMode()
    }

    fun increase() {
        when (rangeStream.value) {
            DatePickerRange.DAY -> dateStream.value = dateStream.value + 86400
            DatePickerRange.MONTH -> {
                if (monthStream.value + 1 > 11) {
                    monthStream.value = 0
                    yearStream.value += 1
                } else {
                    monthStream.value += 1
                }
            }
            DatePickerRange.YEAR -> yearStream.value += 1
        }

        updateDisplayMode()
    }
}