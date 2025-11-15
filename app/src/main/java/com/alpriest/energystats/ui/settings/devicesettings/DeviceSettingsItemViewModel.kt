package com.alpriest.energystats.ui.settings.devicesettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.models.DeviceSettingsItem
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface DirtyStateProviding {
    val dirtyState: StateFlow<Boolean>
}

class DeviceSettingsItemViewModel(
    private val config: ConfigManaging,
    private val network: Networking,
    val item: DeviceSettingsItem
) : ViewModel(), AlertDialogMessageProviding, DirtyStateProviding {
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)

    private val _uiState = MutableStateFlow<LoadState>(LoadState.Inactive)
    val uiState: StateFlow<LoadState> = _uiState

    private val _dirtyState = MutableStateFlow(false)
    override val dirtyState: StateFlow<Boolean> = _dirtyState

    private val _viewDataStream = MutableStateFlow(DeviceSettingItemViewData("",""))
    val viewDataStream: StateFlow<DeviceSettingItemViewData> = _viewDataStream

    private var remoteValue: DeviceSettingItemViewData? = null

    init {
        viewModelScope.launch {
            viewDataStream.collect {
                _dirtyState.value = remoteValue != it
            }
        }
    }

    fun load() {
        if (_uiState.value != LoadState.Inactive) {
            return
        }
        val selectedDeviceSN = config.selectedDeviceSN ?: return
        _uiState.value = LoadState.Active("Loading")

        viewModelScope.launch {
            try {
                val response = network.fetchDeviceSettingsItem(selectedDeviceSN, item)

                val viewData = DeviceSettingItemViewData(response.value, response.unit ?: item.fallbackUnit())
                remoteValue = viewData
                _viewDataStream.value = viewData
                _uiState.value = LoadState.Inactive
            } catch (e: Exception) {
                _uiState.value = LoadState.Error(e, "Failed to load data")
            }
        }
    }
    fun save() {
        if (_uiState.value != LoadState.Inactive) {
            return
        }
        val selectedDeviceSN = config.selectedDeviceSN ?: return
        _uiState.value = LoadState.Active("Saving")

        viewModelScope.launch {
            try {
                network.setDeviceSettingsItem(selectedDeviceSN, item, viewDataStream.value.value)
                remoteValue = _viewDataStream.value
                _uiState.value = LoadState.Inactive
            } catch (e: Exception) {
                _uiState.value = LoadState.Error(e, "Failed to save data")
            }
        }
    }
}