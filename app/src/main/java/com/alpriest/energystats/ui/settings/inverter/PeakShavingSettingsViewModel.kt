package com.alpriest.energystats.ui.settings.inverter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.services.FoxServerError
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PeakShavingSettingsViewModel(
    private val configManager: ConfigManaging, private val networking: Networking
) : ViewModel() {
    private val _state = MutableStateFlow<LoadState>(LoadState.Inactive)
    val uiState: StateFlow<LoadState> = _state

    private val _importLimit = MutableStateFlow("")
    var importLimit: MutableStateFlow<String> = _importLimit

    private val _soc = MutableStateFlow("")
    var soc: MutableStateFlow<String> = _soc

    private val _supported = MutableStateFlow(false)
    var supported: MutableStateFlow<Boolean> = _supported

    fun load(context: Context) {
        val selectedDeviceSN = configManager.selectedDeviceSN ?: return
        if (_state.value is LoadState.Error) return

        _state.value = LoadState.Active(context.getString(R.string.loading))

        viewModelScope.launch {
            try {
                val settings = networking.fetchPeakShavingSettings(selectedDeviceSN)

                _importLimit.value = settings.importLimit.value.removingEmptyDecimals()
                _soc.value = settings.soc.value
                _supported.value = true

                _state.value = LoadState.Inactive
            } catch (e: Exception) {
                val errorMessage = context.getString(R.string.failed_to_load_settings)
                _state.value = when (e) {
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
        if (_state.value != LoadState.Inactive) {
            return
        }
        val selectedDeviceSN = configManager.selectedDeviceSN ?: return
        _state.value = LoadState.Active(context.getString(R.string.saving))

        viewModelScope.launch {
            try {
                networking.setPeakShavingSettings(selectedDeviceSN, importLimit.value.toDouble(), soc.value.toInt())

                _state.value = LoadState.Inactive
            } catch (e: Exception) {
                _state.value = LoadState.Error(e, context.getString(R.string.failed_to_save_settings))
            }
        }
    }
}