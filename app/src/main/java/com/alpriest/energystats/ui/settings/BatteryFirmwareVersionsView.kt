package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.ui.theme.DimmedTextColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BatteryFirmwareVersionsViewModelFactory(
    private val configManager: ConfigManaging,
    private val networking: Networking
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BatteryFirmwareVersionsViewModel(configManager, networking) as T
    }
}

class BatteryFirmwareVersionsView(
    private val configManager: ConfigManaging,
    private val network: Networking
) {

    @Composable
    fun Content(modifier: Modifier, viewModel: BatteryFirmwareVersionsViewModel = viewModel(factory = BatteryFirmwareVersionsViewModelFactory(configManager, network))) {
        LaunchedEffect(null) {
            viewModel.load()
        }

        when (val loadState = viewModel.uiState.collectAsState().value) {
            is LoadState.Inactive -> {
                val batteries = viewModel.modules.collectAsState().value

                if (batteries.isEmpty()) {
                    Text(stringResource(R.string.no_battery_information_available))
                } else {
                    SettingsPage(modifier) {
                        SettingsColumn {
                            batteries.forEachIndexed { index, battery ->
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = "SN: ${battery.batterySN}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.type, battery.type),
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f),
                                            color = DimmedTextColor,
                                        )
                                        Text(
                                            text = stringResource(R.string.version, battery.version),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = DimmedTextColor,
                                        )
                                    }
                                }

                                if (index < batteries.count() - 1) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
            is LoadState.Active -> {
                CircularProgressIndicator()
            }
            is LoadState.Error -> {
                Text("Error: ${loadState}")
            }
        }
    }
}

class BatteryFirmwareVersionsViewModel(
    private val config: ConfigManaging,
    private val network: Networking
) : ViewModel() {

    private val _state = MutableStateFlow<LoadState>(LoadState.Inactive)
    val uiState: StateFlow<LoadState> = _state

    private val _modules = MutableStateFlow<List<DeviceBatteryModule>>(emptyList())
    val modules: StateFlow<List<DeviceBatteryModule>> = _modules

    fun load() {
        val selectedDeviceSN = config.selectedDeviceSN ?: return
        if (_modules.value.isNotEmpty() || _state.value is LoadState.Error) return

        _state.value = LoadState.Active.Loading

        viewModelScope.launch {
            try {
                val device = network.fetchDevice(selectedDeviceSN)

                device.batteryList?.let { batteryList ->
                    _modules.value = batteryList.map {
                        DeviceBatteryModule(it.batterySN, it.type, it.version)
                    }
                    _state.value = LoadState.Inactive
                } ?: run {
                    _state.value = LoadState.Error(null, "Failed to fetch battery information")
                }
            } catch (e: Exception) {
                _state.value = LoadState.Error(e, "Failed to fetch battery information")
            }
        }
    }
}

data class DeviceBatteryModule(
    val batterySN: String,
    val type: String,
    val version: String
)