package com.alpriest.energystats.ui.statsgraph

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private fun kotlinx.coroutines.flow.Flow<Boolean>.stateInEagerFalse(scope: CoroutineScope): StateFlow<Boolean> =
    stateIn(scope, SharingStarted.Eagerly, false)

enum class CustomDateRangePickerError {
    NO_ERROR,
    START_DATE_AFTER_END_DATE,
    TIME_PERIOD_NEEDS_MONTHS
}

class CustomDateRangePickerViewModel : ViewModel() {
    private val _initialStart = MutableStateFlow<LocalDate?>(null)
    private val _initialEnd = MutableStateFlow<LocalDate?>(null)

    private val _start = MutableStateFlow(LocalDate.now())
    private val _end = MutableStateFlow(LocalDate.now())
    private val _viewBy = MutableStateFlow(CustomDateRangeDisplayUnit.MONTHS)
    private val _errorStateStream = MutableStateFlow(CustomDateRangePickerError.NO_ERROR)
    private val _dirty = MutableStateFlow(false)

    val start: StateFlow<LocalDate> = _start.asStateFlow()
    val end: StateFlow<LocalDate> = _end.asStateFlow()
    val viewBy: StateFlow<CustomDateRangeDisplayUnit> = _viewBy.asStateFlow()
    val errorState: StateFlow<CustomDateRangePickerError> = _errorStateStream.asStateFlow()
    val dirty: StateFlow<Boolean> = _dirty.asStateFlow()

    init {
        viewModelScope.launch {
            combine(_initialStart, _initialEnd, _start, _end, _viewBy) { initialStart, initialEnd, start, end, viewBy ->
                recompute(initialStart, initialEnd, start, end, viewBy)
            }.collect { }
        }
    }

    private fun makeErrorMessage(start: LocalDate, end: LocalDate, viewBy: CustomDateRangeDisplayUnit): CustomDateRangePickerError {
        if (start >= end) {
            return CustomDateRangePickerError.START_DATE_AFTER_END_DATE
        }

        val daysBetween = ChronoUnit.DAYS.between(start, end)

        if (viewBy == CustomDateRangeDisplayUnit.DAYS && daysBetween > 45) {
            return CustomDateRangePickerError.TIME_PERIOD_NEEDS_MONTHS
        }

        return CustomDateRangePickerError.NO_ERROR
    }

    fun recompute(initialStart: LocalDate?, initialEnd: LocalDate?, start: LocalDate, end: LocalDate, viewBy: CustomDateRangeDisplayUnit) {
        _errorStateStream.value = makeErrorMessage(start, end, viewBy)
        recomputeDirty(initialStart, initialEnd, start, end, viewBy)
    }

    fun recomputeDirty(initialStart: LocalDate?, initialEnd: LocalDate?, start: LocalDate, end: LocalDate, viewBy: CustomDateRangeDisplayUnit) {
        if (initialStart == null || initialEnd == null) return
        val daysBetween = ChronoUnit.DAYS.between(start, end)

        val dateRangeValid = when (viewBy) {
            CustomDateRangeDisplayUnit.DAYS -> daysBetween < 45
            CustomDateRangeDisplayUnit.MONTHS -> true
        }

        _dirty.value = (start != initialStart || end != initialEnd) && dateRangeValid
    }

    fun initialise(initialStart: LocalDate, initialEnd: LocalDate, initialViewBy: CustomDateRangeDisplayUnit) {
        // Only initialise once per instance.
        if (_initialStart.value != null && _initialEnd.value != null) return

        _initialStart.value = initialStart
        _initialEnd.value = initialEnd
        _viewBy.value = initialViewBy

        // Normalise according to viewBy on init.
        setStart(initialStart)
        setEnd(initialEnd)
    }

    fun setViewBy(value: CustomDateRangeDisplayUnit) {
        _viewBy.value = value
        // Re-normalise existing dates when switching modes.
        setStart(_start.value)
        setEnd(_end.value)
    }

    fun setStart(value: LocalDate) {
        _start.value = if (_viewBy.value == CustomDateRangeDisplayUnit.MONTHS) value.withDayOfMonth(1) else value
    }

    fun setEnd(value: LocalDate) {
        _end.value = if (_viewBy.value == CustomDateRangeDisplayUnit.MONTHS) value.withDayOfMonth(value.lengthOfMonth()) else value
    }

    fun setLastMonths(count: Int, context: Context) {
        // Uses full calendar months ending at the end of the current month.
        val now = LocalDate.now()
        val newEnd = now.plusMonths(1).withDayOfMonth(1).minusDays(1)
        val newStart = newEnd.minusMonths((count - 1).toLong()).withDayOfMonth(1)
        setViewBy(CustomDateRangeDisplayUnit.MONTHS)
        _start.value = newStart
        _end.value = newEnd
    }
}
