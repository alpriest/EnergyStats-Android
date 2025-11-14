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

    val valueStream = MutableStateFlow("")

    private val _unitStream = MutableStateFlow("")
    val unitStream: StateFlow<String> = _unitStream

    fun load() {
        if (_uiState.value != LoadState.Inactive) {
            return
        }
        val selectedDeviceSN = config.selectedDeviceSN ?: return
        _uiState.value = LoadState.Active("Loading")

        viewModelScope.launch {
            try {
                val response = network.fetchDeviceSettingsItem(selectedDeviceSN, item)

                _unitStream.value = response.unit ?: item.fallbackUnit()
                valueStream.value = response.value
                _uiState.value = LoadState.Inactive
            } catch (e: Exception) {
                _uiState.value = LoadState.Error(e, "Failed to fetch battery information")
            }
        }
    }

    fun save() {
        if (_uiState.value != LoadState.Inactive) {
            return
        }
        val selectedDeviceSN = config.selectedDeviceSN ?: return
        _uiState.value = LoadState.Active("Loading")

        viewModelScope.launch {
            try {
                network.setDeviceSettingsItem(selectedDeviceSN, item, valueStream.value)

                _uiState.value = LoadState.Inactive
            } catch (e: Exception) {
                _uiState.value = LoadState.Error(e, "Failed to fetch battery information")
            }
        }
    }
}