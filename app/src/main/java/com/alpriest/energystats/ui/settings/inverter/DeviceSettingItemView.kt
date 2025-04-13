package com.alpriest.energystats.ui.settings.inverter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.models.DeviceSettingsItem
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsPage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeviceSettingsItemViewViewModelFactory(
    private val configManager: ConfigManaging,
    private val networking: Networking,
    private val item: DeviceSettingsItem
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DeviceSettingsItemViewViewModel(configManager, networking, item) as T
    }
}

class DeviceSettingsItemViewViewModel(
    private val config: ConfigManaging,
    private val network: Networking,
    val item: DeviceSettingsItem
) : ViewModel() {
    private val _state = MutableStateFlow<LoadState>(LoadState.Inactive)
    val uiState: StateFlow<LoadState> = _state

    val valueStream = MutableStateFlow<String>("")

    private val _unitStream = MutableStateFlow<String>("")
    val unitStream: StateFlow<String> = _unitStream

    fun load() {
        val selectedDeviceSN = config.selectedDeviceSN ?: return

        _state.value = LoadState.Active("Loading")

        viewModelScope.launch {
            try {
                val response = network.fetchDeviceSettingsItem(selectedDeviceSN, item)

                _unitStream.value = response.unit ?: item.fallbackUnit()
//                device.batteryList?.let { batteryList ->
//                    _modules.value = batteryList.map {
//                        DeviceBatteryModule(it.batterySN, it.type, it.version)
//                    }
//                    _state.value = LoadState.Inactive
//                } ?: run {
//                    _state.value = LoadState.Error(null, "Failed to fetch battery information")
//                }
            } catch (e: Exception) {
                _state.value = LoadState.Error(e, "Failed to fetch battery information")
            }
        }
    }
}

class DeviceSettingItemView(
    private val configManager: ConfigManaging,
    private val network: Networking,
    private val item: DeviceSettingsItem
) {
    @Composable
    fun Content(modifier: Modifier, viewModel: DeviceSettingsItemViewViewModel = viewModel(factory = DeviceSettingsItemViewViewModelFactory(configManager, network, item))) {
        LaunchedEffect(null) {
            viewModel.load()
        }

        when (val loadState = viewModel.uiState.collectAsState().value) {
            is LoadState.Inactive -> {
                LoadedView(viewModel, modifier)
            }
            is LoadState.Active -> {
                CircularProgressIndicator()
            }
            is LoadState.Error -> {
                Text("Error: $loadState")
            }
        }
    }

    @Composable
    private fun LoadedView(viewModel: DeviceSettingsItemViewViewModel, modifier: Modifier) {
        val context = LocalContext.current
        val value = viewModel.valueStream.collectAsState().value
        val unit = viewModel.unitStream.collectAsState().value

        SettingsPage(modifier) {
            SettingsColumn {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        viewModel.item.title(context),
                        Modifier.weight(1.0f),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    OutlinedTextField(
                        value = value,
                        onValueChange = { viewModel.valueStream.value = it.filter { it.isDigit() } },
                        modifier = Modifier.width(100.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSecondary),
                        trailingIcon = { Text(unit, color = MaterialTheme.colorScheme.onSecondary) }
                    )
                }
            }
        }
    }
}