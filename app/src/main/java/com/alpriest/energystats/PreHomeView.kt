package com.alpriest.energystats

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.helpers.AlertDialogMessageProviding
import com.alpriest.energystats.helpers.WearableApiAvailability
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.shared.models.Schedule
import com.alpriest.energystats.shared.models.SchedulePhase
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.ui.AppContainer
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.settings.inverter.schedule.asSchedule
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val CREDS_PATH = "/auth/creds"

class PreHomeViewModel(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val solarForecastProvider: () -> SolcastCaching,
    private val credentialStore: CredentialStore
) : ViewModel(), AlertDialogMessageProviding {
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)

    internal fun loadData(context: Context) {
        viewModelScope.launch {
            try {
                network.fetchErrorMessages()
            } catch (_: Exception) {
            }
        }

        viewModelScope.launch {
            refreshSolcast()
        }

        viewModelScope.launch {
            fetchCurrentInverterSchedule()
        }

        viewModelScope.launch {
            credentialStore.getApiKey()?.let {
                pushCredentialsToWatch(context, it)
            }
        }
    }

    suspend fun pushCredentialsToWatch(context: Context, token: String) {
        val dataClient = Wearable.getDataClient(context)

        if (WearableApiAvailability.isAvailable(dataClient)) {
            val putReq = PutDataMapRequest.create(CREDS_PATH).apply {
                dataMap.putString("token", token)

                // Force propagation even if token string hasn’t changed
                dataMap.putLong("updatedAt", System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()

            dataClient.putDataItem(putReq).await()
        }
    }

    private suspend fun refreshSolcast() {
        if (!configManager.fetchSolcastOnAppLaunch) return
        val apiKey = configManager.solcastSettings.apiKey ?: return

        val service = solarForecastProvider()

        try {
            configManager.solcastSettings.sites.forEach { site ->
                service.fetchForecast(site, apiKey, ignoreCache = false)
            }
        } catch (_: Exception) {
            // Ignore
        }
    }

    private suspend fun fetchCurrentInverterSchedule() {
        if (!configManager.showInverterScheduleQuickLink) return
        val deviceSN = configManager.selectedDeviceSN ?: return
        try {
            val scheduleResponse = network.fetchCurrentSchedule(deviceSN)
            val schedule = Schedule.create(scheduleResponse)

            configManager.scheduleTemplates.forEach { template ->
                val templatePhases = template.asSchedule().phases
                    .sortedWith { first, second -> first.start.compareTo(second.start) }
                val match = templatePhases.zip(schedule.phases).all { (templatePhase, schedulePhase) ->
                    templatePhase.isEqualConfiguration(schedulePhase)
                }
                if (match) {
                    val appTheme = configManager.themeStream.value.copy(detectedActiveTemplate = template.name)
                    configManager.themeStream.value = appTheme
                }
            }
        } catch (_: Exception) {
            // Ignore
        }
    }

    private fun SchedulePhase.isEqualConfiguration(other: SchedulePhase): Boolean {
        return start == other.start &&
                end == other.end &&
                mode == other.mode &&
                minSocOnGrid == other.minSocOnGrid &&
                forceDischargePower == other.forceDischargePower &&
                forceDischargeSOC == other.forceDischargeSOC &&
                maxSOC == other.maxSOC
    }
}

@Composable
fun PreHomeView(appContainer: AppContainer, viewModel: PreHomeViewModel) {
    MonitorAlertDialog(viewModel, appContainer.userManager)
    val context = LocalContext.current

    LaunchedEffect(null) {
        viewModel.loadData(context)
    }

    MainAppView(appContainer)
}
