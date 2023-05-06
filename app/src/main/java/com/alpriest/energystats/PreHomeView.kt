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
    val _uiState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val uiState: StateFlow<Boolean> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            configManager.fetchFirmwareVersions()
            configManager.fetchVariables()
            _uiState.value = true
        }
    }
}

@Composable
fun PreHomeView(appContainer: AppContainer, viewModel: PreHomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        false -> LoadingView(title = "Loading...")
        true -> MainAppView(appContainer)
    }
}
