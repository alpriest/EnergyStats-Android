package com.alpriest.energystats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.SharedPreferencesConfigStore
import com.alpriest.energystats.ui.AppContainer
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.login.LoggedIn
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PreHomeViewModel(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val userManager: UserManaging,
    private val config: SharedPreferencesConfigStore
) : ViewModel(), AlertDialogMessageProviding {
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)

    internal fun loadData() {
        viewModelScope.launch {
            try {
                network.fetchErrorMessages()

                if (config.powerStationDetail == null) {
                    configManager.fetchPowerStationDetail()
                }

                if (userManager.loggedInState.value.loadState == LoggedIn) {
                    configManager.fetchDevices()
                }
            } catch (ex: Exception) {
                alertDialogMessage.value = MonitorAlertDialogData(ex, ex.localizedMessage)
            }
        }
    }
}

@Composable
fun PreHomeView(appContainer: AppContainer, viewModel: PreHomeViewModel) {
    MonitorAlertDialog(viewModel, appContainer.userManager)

    LaunchedEffect(null) {
        viewModel.loadData()
    }

    MainAppView(appContainer)
}
