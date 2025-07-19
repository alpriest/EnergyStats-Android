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
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.settings.inverter.schedule.SchedulePhase
import com.alpriest.energystats.ui.settings.inverter.schedule.asSchedule
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

        viewModelScope.launch {
            fetchCurrentInverterSchedule()
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
                maxSoc == other.maxSoc
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
