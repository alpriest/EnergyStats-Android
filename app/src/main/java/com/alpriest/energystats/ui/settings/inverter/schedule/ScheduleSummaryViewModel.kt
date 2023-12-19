package com.alpriest.energystats.ui.settings.inverter.schedule

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.SchedulePollcy
import com.alpriest.energystats.models.ScheduleTemplateSummaryResponse
import com.alpriest.energystats.models.SchedulerModeResponse
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.settings.SettingsScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ScheduleSummaryViewModelFactory(
    private val network: FoxESSNetworking,
    private val configManager: ConfigManaging,
    private val navController: NavController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ScheduleSummaryViewModel(network, configManager, navController) as T
    }
}

class ScheduleSummaryViewModel(
    val network: FoxESSNetworking,
    val config: ConfigManaging,
    val navController: NavController
) : ViewModel(), AlertDialogMessageProviding {
    val scheduleStream = MutableStateFlow<Schedule?>(null)
    val supportedStream = MutableStateFlow(false)
    private var modes = EditScheduleStore.shared.modes
    val templateStream = MutableStateFlow<List<ScheduleTemplateSummary>>(listOf())
    val uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    override val alertDialogMessage = MutableStateFlow<String?>(null)

    private suspend fun preload(context: Context) {
        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceID = device.deviceID
                val deviceSN = device.deviceSN
                uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

                try {
                    supportedStream.value = network.fetchSchedulerFlag(deviceSN).support
                    if (supportedStream.value) {
                        EditScheduleStore.shared.modes = network.fetchScheduleModes(deviceID)
                        modes = EditScheduleStore.shared.modes
                        uiState.value = UiLoadState(LoadState.Inactive)
                    } else {
                        uiState.value = UiLoadState(LoadState.Inactive)
                    }
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex.localizedMessage ?: "Unknown error"))
                }
            } ?: {
                uiState.value = UiLoadState(LoadState.Inactive)
            }
        }
    }

    suspend fun load(context: Context) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        if (modes.isEmpty()) {
            preload(context)
        }

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

                try {
                    val scheduleResponse = network.fetchCurrentSchedule(deviceSN)
                    templateStream.value = scheduleResponse.data.mapNotNull { it.toScheduleTemplate() }
                    scheduleStream.value = Schedule(name = "", phases = scheduleResponse.pollcy.mapNotNull { it.toSchedulePhase(modes) }, description = null)

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex.localizedMessage ?: "Unknown error"))
                }
            }
        }
    }

    fun createSchedule() {
        val schedule = scheduleStream.value ?: return

        EditScheduleStore.shared.scheduleStream.value = schedule
        EditScheduleStore.shared.phaseId = null
        EditScheduleStore.shared.allowDeletion = false

        navController.navigate(SettingsScreen.EditSchedule.name)
    }

    fun editSchedule() {
        val schedule = scheduleStream.value ?: return

        EditScheduleStore.shared.scheduleStream.value = schedule
        EditScheduleStore.shared.phaseId = null
        EditScheduleStore.shared.allowDeletion = true

        navController.navigate(SettingsScreen.EditSchedule.name)
    }

    fun activate(scheduleTemplate: ScheduleTemplateSummary, context: Context) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        viewModelScope.launch {
            runCatching {
                config.currentDevice.value?.let { device ->
                    val deviceSN = device.deviceSN

                    uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.activating)))

                    try {
                        network.enableScheduleTemplate(deviceSN, scheduleTemplate.id)
                        uiState.value = UiLoadState(LoadState.Inactive)
                        load(context)
                    } catch (ex: Exception) {
                        uiState.value = UiLoadState(LoadState.Error(ex.localizedMessage ?: "Unknown error"))
                    }
                }
            }
        }
    }
}

internal fun SchedulePollcy.toSchedulePhase(modes: List<SchedulerModeResponse>): SchedulePhase? {
    return SchedulePhase.create(
        start = Time(hour = startH, minute = startM),
        end = Time(hour = endH, minute = endM),
        mode = modes.first { it.key == workMode },
        forceDischargePower = fdpwr ?: 0,
        forceDischargeSOC = fdsoc,
        batterySOC = minsocongrid,
        color = Color.scheduleColor(workMode)
    )
}

internal fun ScheduleTemplateSummaryResponse.toScheduleTemplate(): ScheduleTemplateSummary? {
    if (templateID.isEmpty()) {
        return null
    }

    return ScheduleTemplateSummary(
        templateID,
        templateName,
        enable
    )
}

