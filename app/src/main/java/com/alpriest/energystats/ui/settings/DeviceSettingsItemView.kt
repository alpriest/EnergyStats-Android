package com.alpriest.energystats.ui.settings

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.models.DeviceSettingsItem
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DeviceSettingsItemViewViewModelFactory(
    private val configManager: ConfigManaging,
    private val networking: Networking
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DeviceSettingsItemViewViewModel(configManager, networking) as T
    }
}

class DeviceSettingsItemViewViewModel(
    private val config: ConfigManaging,
    private val network: Networking
) : ViewModel() {
    private val _state = MutableStateFlow<LoadState>(LoadState.Inactive)
    val uiState: StateFlow<LoadState> = _state

    fun load() {
        val selectedDeviceSN = config.selectedDeviceSN ?: return
    }
}

class DeviceSettingsItemView(
    private val configManager: ConfigManaging,
    private val network: Networking,
    private val item: DeviceSettingsItem
) {
    @Composable
    fun Content(modifier: Modifier, viewModel: DeviceSettingsItemViewViewModel = viewModel(factory = DeviceSettingsItemViewViewModelFactory(configManager, network))) {
        LaunchedEffect(null) {
            viewModel.load()
        }

        when (val loadState = viewModel.uiState.collectAsState().value) {
            is LoadState.Inactive -> {
                LoadedView(modifier)
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
    private fun LoadedView(modifier: Modifier) {
        SettingsPage(modifier) {
            SettingsColumn {
                Text("Hello")
            }
        }
    }
}