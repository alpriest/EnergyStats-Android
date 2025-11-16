package com.alpriest.energystats.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.stores.ConfigManaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class EditSummaryViewModelFactory(
    private val configManager: ConfigManaging,
    private val onChange: (SummaryDateRange) -> Unit
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditSummaryViewModel(configManager, onChange) as T
    }
}

class EditSummaryViewModel(
    configManager: ConfigManaging,
    private val onChange: (SummaryDateRange) -> Unit
) : ViewModel() {
    private val _viewDataStream: MutableStateFlow<EditSummaryViewData>
    val viewDataStream: StateFlow<EditSummaryViewData>

    private val _dirtyState = MutableStateFlow(false)
    val dirtyState: StateFlow<Boolean> = _dirtyState

    private var remoteValue: EditSummaryViewData? = null

    init {
        val summaryDateRange = configManager.summaryDateRange
        val viewData = when (summaryDateRange) {
            is SummaryDateRange.Automatic -> EditSummaryViewData(
                automatic = true,
                fromMonth = 1,
                fromYear = 2020,
                toMonth = LocalDate.now().monthValue,
                toYear = LocalDate.now().year
            )

            is SummaryDateRange.Manual -> EditSummaryViewData(
                automatic = false,
                fromMonth = summaryDateRange.from.month,
                fromYear = summaryDateRange.from.year,
                toMonth = summaryDateRange.to.month,
                toYear = summaryDateRange.to.year
            )
        }
        _viewDataStream = MutableStateFlow(viewData)
        viewDataStream = _viewDataStream
        remoteValue = viewDataStream.value
        viewModelScope.launch {
            viewDataStream.collect {
                _dirtyState.value = remoteValue != it
            }
        }
    }

    fun save() {
        val viewData = _viewDataStream.value
        val updatedSummaryDateRange = if (viewData.automatic) {
            SummaryDateRange.Automatic
        } else {
            SummaryDateRange.Manual(
                from = MonthYear(viewData.fromMonth, viewData.fromYear),
                to = MonthYear(viewData.toMonth, viewData.toYear)
            )
        }
        onChange(updatedSummaryDateRange)
    }

    fun didChangeAutomatic(it: Boolean) {
        _viewDataStream.value = viewDataStream.value.copy(automatic = it)
    }

    fun didChangeFromMonth(it: Int) {
        _viewDataStream.value = viewDataStream.value.copy(fromMonth = it)
    }

    fun didChangeFromYear(it: Int) {
        _viewDataStream.value = viewDataStream.value.copy(fromYear = it)
    }

    fun didChangeToMonth(it: Int) {
        _viewDataStream.value = viewDataStream.value.copy(toMonth = it)
    }

    fun didChangeToYear(it: Int) {
        _viewDataStream.value = viewDataStream.value.copy(toYear = it)
    }
}