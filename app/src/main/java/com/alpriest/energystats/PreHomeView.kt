package com.alpriest.energystats

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.AppContainer
import com.alpriest.energystats.ui.login.LoggedIn
import com.alpriest.energystats.ui.login.UserManaging
import kotlinx.coroutines.launch

class PreHomeViewModel(
    private val network: FoxESSNetworking,
    private val configManager: ConfigManaging,
    private val userManager: UserManaging
) : ViewModel() {
    internal fun loadData() {
        viewModelScope.launch {
            network.fetchErrorMessages()

            try {
                if (userManager.loggedInState.value.loadState == LoggedIn) {
                    configManager.fetchDevices()
                    configManager.refreshFirmwareVersions()
                }
            } catch (ex: Exception) {
                print(ex.localizedMessage)
            }
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
