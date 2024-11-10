package com.alpriest.energystats.ui.settings.inverter.schedule

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.DeviceFirmwareVersion
import com.alpriest.energystats.models.SchedulePhaseResponse
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.settings.SettingsScreen
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.TemplateStoring
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ScheduleSummaryViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController,
    private val templateStore: TemplateStoring
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ScheduleSummaryViewModel(network, configManager, navController, templateStore) as T
    }
}

class ScheduleSummaryViewModel(
    val network: Networking,
    val config: ConfigManaging,
    val navController: NavController,
    val templateStore: TemplateStoring
) : ViewModel(), AlertDialogMessageProviding {
    val scheduleStream = MutableStateFlow<Schedule?>(null)
    val supportedErrorStream = MutableStateFlow<String?>(null)
    val templateStream = MutableStateFlow<List<ScheduleTemplate>>(listOf())
    val uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    private var hasPreloaded = false
    val schedulerEnabledStream = MutableStateFlow(false)

    private suspend fun preload(context: Context) {
        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN
                uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

                try {
                    val flags = network.fetchSchedulerFlag(deviceSN)

                    if (!flags.support) {
                        val firmwareVersion: DeviceFirmwareVersion? = try {
                            val response = network.fetchDevice(deviceSN)
                            DeviceFirmwareVersion(master = response.masterVersion, manager = response.managerVersion, slave = response.slaveVersion)
                        } catch (ex: Exception) {
                            null
                        }
                        supportedErrorStream.value = String.format(
                            context.getString(R.string.unsupported_firmware),
                            device.deviceType,
                            firmwareVersion?.manager ?: ""
                        )
                        uiState.value = UiLoadState(LoadState.Inactive)
                    } else {
                        uiState.value = UiLoadState(LoadState.Inactive)
                    }
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: context.getString(R.string.unknown_error)))
                }
            } ?: {
                uiState.value = UiLoadState(LoadState.Inactive)
            }
        }
    }

    suspend fun load(context: Context) {
        when (uiState.value.state) {
            is LoadState.Active -> return
            else -> {}
        }

        if (!hasPreloaded) {
            preload(context)
            hasPreloaded = true

            if (supportedErrorStream.value != null || uiState.value.state is LoadState.Error) {
                return
            }
        }

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

                try {
                    val scheduleResponse = network.fetchCurrentSchedule(deviceSN)
                    templateStream.value = templateStore.load()
                    scheduleStream.value = Schedule(name = "", phases = scheduleResponse.groups.mapNotNull { it.toSchedulePhase() })
                    schedulerEnabledStream.value = scheduleResponse.enable == 1

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: context.getString(R.string.unknown_error)))
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

    fun setSchedulerFlag(context: Context, schedulerEnabled: Boolean) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        viewModelScope.launch {
            runCatching {
                config.currentDevice.value?.let { device ->
                    val deviceSN = device.deviceSN

                    if (schedulerEnabled) {
                        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.activating)))
                    } else {
                        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.deactivating)))
                    }

                    try {
                        network.setScheduleFlag(deviceSN, schedulerEnabled)
                        schedulerEnabledStream.value = schedulerEnabled
                        uiState.value = UiLoadState(LoadState.Inactive)
                    } catch (ex: Exception) {
                        uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: context.getString(R.string.unknown_error)))
                    }
                }
            }
        }
    }

    fun editTemplate(template: ScheduleTemplate) {
        EditScheduleStore.shared.reset()
        EditScheduleStore.shared.templateStream.value = template
        EditScheduleStore.shared.allowDeletion = true

        navController.navigate(SettingsScreen.EditTemplate.name)
    }

    fun activate(template: ScheduleTemplate, context: Context) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }
        val schedule = template.asSchedule()

        if (!schedule.isValid()) {
            alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.battery_periods_overlap))
            return
        }

        viewModelScope.launch {
            runCatching {
                config.currentDevice.value?.let { device ->
                    val deviceSN = device.deviceSN
                    try {
                        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.activating)))
                        network.saveSchedule(deviceSN, schedule)
                        uiState.value = UiLoadState(LoadState.Inactive)
                        load(context)
                    } catch (ex: Exception) {
                        uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: context.getString(R.string.unknown_error), false))
                    }
                }
            }
        }
    }

    fun clearError() {
        uiState.value = UiLoadState(LoadState.Inactive)
    }
}

internal fun SchedulePhaseResponse.toSchedulePhase(): SchedulePhase? {
    if (workMode == WorkMode.Invalid) { return null }
    if (enable == 0) { return null }

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

