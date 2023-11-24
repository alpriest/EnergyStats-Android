package com.alpriest.energystats

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.AppContainer
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.login.LoggedIn
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PreHomeViewModel(
    private val network: FoxESSNetworking,
    private val configManager: ConfigManaging,
    private val userManager: UserManaging
) : ViewModel(), AlertDialogMessageProviding {
    override val alertDialogMessage = MutableStateFlow<String?>(null)

    internal fun loadData(context: Context) {
        viewModelScope.launch {
            try {
                network.fetchErrorMessages()

                if (userManager.loggedInState.value.loadState == LoggedIn) {
                    configManager.fetchDevices()
                    configManager.refreshFirmwareVersions()
                }
            } catch (ex: Exception) {
                alertDialogMessage.value = ex.localizedMessage
            }
        }
    }
}

@Composable
fun PreHomeView(appContainer: AppContainer, viewModel: PreHomeViewModel) {
    val context = LocalContext.current

    MonitorAlertDialog(viewModel)

    LaunchedEffect(null) {
        viewModel.loadData(context)
    }

    MainAppView(appContainer)
}
