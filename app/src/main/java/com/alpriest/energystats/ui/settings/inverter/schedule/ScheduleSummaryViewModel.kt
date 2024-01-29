package com.alpriest.energystats.ui.settings.inverter.schedule

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.SchedulePhaseResponse
import com.alpriest.energystats.models.ScheduleTemplateSummaryResponse
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.settings.SettingsScreen
import com.alpriest.energystats.ui.settings.inverter.deviceDisplayName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.min

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
    val supportedErrorStream = MutableStateFlow<String?>(null)
    val templateStream = MutableStateFlow<List<ScheduleTemplateSummary>>(listOf())
    val uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    var hasPreloaded = false

    private suspend fun preload(context: Context) {
        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN
                uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

                try {

                    val flags = network.openapi_fetchSchedulerFlag(deviceSN)

                    if (!flags.support) {
                        supportedErrorStream.value = String.format(
                            context.getString(R.string.unsupported_firmware),
                            device.deviceDisplayName,
                            device.firmware?.manager ?: ""
                        )
                        uiState.value = UiLoadState(LoadState.Inactive)
                    } else {
                        uiState.value = UiLoadState(LoadState.Inactive)
                    }
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: "Unknown error"))
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

        if (!hasPreloaded) {
            preload(context)
            hasPreloaded = true

            if (supportedErrorStream.value != null) {
                return
            }
        }

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

                try {
                    val scheduleResponse = network.openapi_fetchCurrentSchedule(deviceSN)
                    scheduleStream.value = Schedule(name = "", phases = scheduleResponse.groups.mapNotNull { it.toSchedulePhase() }, description = null)

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: "Unknown error"))
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

                    // TODO
//                    try {
//                        network.enableScheduleTemplate(deviceSN, scheduleTemplate.id)
//                        uiState.value = UiLoadState(LoadState.Inactive)
//                        load(context)
//                    } catch (ex: Exception) {
//                        uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: "Unknown error"))
//                    }
                }
            }
        }
    }
}

internal fun SchedulePhaseResponse.toSchedulePhase(): SchedulePhase? {
    if (workMode == WorkMode.Invalid) { return null }

    return SchedulePhase.create(
        start = Time(hour = startHour, minute = startMinute),
        end = Time(hour = endHour, minute = endMinute),
        mode = workMode,
        forceDischargePower = fdPwr ?: 0,
        forceDischargeSOC = fdSoc,
        batterySOC = minSocOnGrid,
        color = Color.scheduleColor(workMode)
    )
}
