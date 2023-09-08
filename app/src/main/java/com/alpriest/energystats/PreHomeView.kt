package com.alpriest.energystats

import android.os.UserManager
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.AppContainer
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.login.LoggedIn
import com.alpriest.energystats.ui.login.UserManaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PreHomeViewModel(
    private val configManager: ConfigManaging,
    private val userManager: UserManaging
) : ViewModel() {
    internal fun loadData() {
        viewModelScope.launch {
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
