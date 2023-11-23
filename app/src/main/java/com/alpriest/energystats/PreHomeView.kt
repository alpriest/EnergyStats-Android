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
import com.alpriest.energystats.ui.login.LoggedIn
import com.alpriest.energystats.ui.login.UserManaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PreHomeViewModel(
    private val network: FoxESSNetworking,
    private val configManager: ConfigManaging,
    private val userManager: UserManaging
) : ViewModel() {
    val toastMessage = MutableStateFlow<String?>(null)

    internal fun loadData(context: Context) {
        viewModelScope.launch {
            try {
                network.fetchErrorMessages()

                if (userManager.loggedInState.value.loadState == LoggedIn) {
                    configManager.fetchDevices()
                    configManager.refreshFirmwareVersions()
                }
            } catch (ex: Exception) {
                toastMessage.value = ex.localizedMessage
            }
        }
    }
}

@Composable
fun PreHomeView(appContainer: AppContainer, viewModel: PreHomeViewModel) {
    val context = LocalContext.current
    val toastMessage = viewModel.toastMessage.collectAsState().value

    toastMessage?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        viewModel.toastMessage.value = null
    }

    LaunchedEffect(null) {
        viewModel.loadData(context)
    }

    MainAppView(appContainer)
}
