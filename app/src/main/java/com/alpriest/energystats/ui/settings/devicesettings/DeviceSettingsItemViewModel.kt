package com.alpriest.energystats.ui.settings.devicesettings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.shared.models.network.DeviceSettingsItem
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.helpers.AlertDialogMessageProviding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeviceSettingsItemViewModel(
    private val config: ConfigManaging,
    private val network: Networking,
    val item: DeviceSettingsItem
) : ViewModel(), AlertDialogMessageProviding {
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)

    private val _uiState = MutableStateFlow<LoadState>(LoadState.Inactive)
    val uiState: StateFlow<LoadState> = _uiState

    private val _dirtyState = MutableStateFlow(false)
    val dirtyState: StateFlow<Boolean> = _dirtyState

    private val _viewDataStream = MutableStateFlow(DeviceSettingItemViewData("",""))
    val viewDataStream: StateFlow<DeviceSettingItemViewData> = _viewDataStream

    private var originalValue: DeviceSettingItemViewData? = null

    init {
        viewModelScope.launch {
            viewDataStream.collect {
                _dirtyState.value = originalValue != it
            }
        }
    }

    fun didChange(value: String) {
        _viewDataStream.value = viewDataStream.value.copy(value = value)
    }

    fun load(context: Context) {
        if (_uiState.value != LoadState.Inactive) { return }
        val selectedDeviceSN = config.selectedDeviceSN ?: return
        _uiState.value = LoadState.Active.Loading

        viewModelScope.launch {
            try {
                val response = network.fetchDeviceSettingsItem(selectedDeviceSN, item)

                val viewData = DeviceSettingItemViewData(response.value, response.unit ?: item.fallbackUnit())
                originalValue = viewData
                _viewDataStream.value = viewData
                _uiState.value = LoadState.Inactive
            } catch (e: Exception) {
                _uiState.value = LoadState.Error(e, "Failed to load data")
            }
        }
    }

    fun save(context: Context) {
        if (_uiState.value != LoadState.Inactive) { return }
        val selectedDeviceSN = config.selectedDeviceSN ?: return
        _uiState.value = LoadState.Active.Saving

        viewModelScope.launch {
            try {
                network.setDeviceSettingsItem(selectedDeviceSN, item, viewDataStream.value.value)
                resetDirtyState()
                _uiState.value = LoadState.Inactive
            } catch (e: Exception) {
                _uiState.value = LoadState.Error(e, "Failed to save data")
            }
        }
    }

    private fun resetDirtyState() {
        originalValue = _viewDataStream.value
        _dirtyState.value = false
    }
}