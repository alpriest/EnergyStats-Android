package com.alpriest.energystats.ui.settings.inverter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.shared.network.FoxServerError
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.shared.models.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PeakShavingSettingsViewData(
    val importLimit: String,
    val soc: String,
    val supported: Boolean
)

class PeakShavingSettingsViewModel(
    private val configManager: ConfigManaging, private val networking: Networking
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoadState>(LoadState.Inactive)
    val uiState: StateFlow<LoadState> = _uiState

    private val _viewDataStream = MutableStateFlow(PeakShavingSettingsViewData("","", false))
    val viewDataStream: StateFlow<PeakShavingSettingsViewData> = _viewDataStream

    private val _dirtyState = MutableStateFlow(false)
    val dirtyState: StateFlow<Boolean> = _dirtyState

    private var originalValue: PeakShavingSettingsViewData? = null

    init {
        viewModelScope.launch {
            viewDataStream.collect {
                _dirtyState.value = originalValue != it
            }
        }
    }

    fun load(context: Context) {
        val selectedDeviceSN = configManager.selectedDeviceSN ?: return
        if (_uiState.value is LoadState.Error) return

        _uiState.value = LoadState.Active.Loading

        viewModelScope.launch {
            try {
                val settings = networking.fetchPeakShavingSettings(selectedDeviceSN)

                val viewData = PeakShavingSettingsViewData(settings.importLimit.value.removingEmptyDecimals(), settings.soc.value, true)
                originalValue = viewData
                _viewDataStream.value = viewData

                _uiState.value = LoadState.Inactive
            } catch (e: Exception) {
                val errorMessage = context.getString(R.string.failed_to_load_settings)
                _uiState.value = when (e) {
                    is FoxServerError -> if (e.errno == 40257) {
                        LoadState.Inactive
                    } else {
                        LoadState.Error(e, errorMessage)
                    }

                    else -> LoadState.Error(e, errorMessage)
                }
            }
        }
    }

    fun save(context: Context) {
        if (_uiState.value != LoadState.Inactive) {
            return
        }
        val selectedDeviceSN = configManager.selectedDeviceSN ?: return
        _uiState.value = LoadState.Active.Saving

        viewModelScope.launch {
            try {
                val viewData = viewDataStream.value
                networking.setPeakShavingSettings(selectedDeviceSN, viewData.importLimit.toDouble(), viewData.soc.toInt())

                _uiState.value = LoadState.Inactive
            } catch (e: Exception) {
                _uiState.value = LoadState.Error(e, context.getString(R.string.failed_to_save_settings))
            }
        }
    }

    fun didChangeImportLimit(value: String) {
        _viewDataStream.value = viewDataStream.value.copy(importLimit = value)
    }

    fun didChangeSoc(value: String) {
        _viewDataStream.value = viewDataStream.value.copy(soc = value)
    }
}