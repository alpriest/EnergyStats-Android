package com.alpriest.energystats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.AppContainer
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PreHomeViewModel(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val solarForecastProvider: () -> SolcastCaching
) : ViewModel(), AlertDialogMessageProviding {
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)

    internal fun loadData() {
        viewModelScope.launch {
            try {
                network.fetchErrorMessages()
            } catch (_: Exception) {
            }
        }

        viewModelScope.launch {
            refreshSolcast()
        }
    }

    private suspend fun refreshSolcast() {
        if (!configManager.fetchSolcastOnAppLaunch) return
        val apiKey = configManager.solcastSettings.apiKey ?: return

        val service = solarForecastProvider()

        try {
            configManager.solcastSettings.sites.map { site ->
                service.fetchForecast(site, apiKey, ignoreCache = false)
            }
        } catch (_: Exception) {
            // Ignore
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
