package com.alpriest.energystats

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.AppContainer
import com.alpriest.energystats.ui.LoadingView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PreHomeViewModel(
    private val configManager: ConfigManaging,
) : ViewModel() {
    internal fun loadData() {
        viewModelScope.launch {
            configManager.fetchDevices()
            configManager.refreshFirmwareVersions()
        }
    }
}

@Composable
fun PreHomeView(appContainer: AppContainer, viewModel: PreHomeViewModel) {
    LaunchedEffect(null) {
        viewModel.loadData()
    }

    MainAppView(appContainer)
}
