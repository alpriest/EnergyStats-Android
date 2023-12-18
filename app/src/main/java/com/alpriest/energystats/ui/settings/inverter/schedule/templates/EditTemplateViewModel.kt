package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.SchedulerModeResponse
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.settings.inverter.schedule.EditScheduleStore
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.settings.inverter.schedule.SchedulePhaseHelper
import com.alpriest.energystats.ui.settings.inverter.schedule.toSchedulePhase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class EditTemplateViewModelFactory(
    private val configManager: ConfigManaging,
    private val network: FoxESSNetworking,
    private val navController: NavHostController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditTemplateViewModel(configManager, network, navController) as T
    }
}

class EditTemplateViewModel(
    val config: ConfigManaging,
    val network: FoxESSNetworking,
    val navController: NavHostController
) : ViewModel(), AlertDialogMessageProviding {
    val scheduleStream = EditScheduleStore.shared.scheduleStream
    override val alertDialogMessage = MutableStateFlow<String?>(null)
    val uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    private var modes: List<SchedulerModeResponse> = listOf()
    private var templateID: String = ""
    private var shouldPopNavOnDismissal = false

    suspend fun load(context: Context) {
        modes = EditScheduleStore.shared.modes
        templateID = EditScheduleStore.shared.templateID ?: return

        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

                try {
                    val template = network.fetchScheduleTemplate(deviceSN, templateID)
                    scheduleStream.value = Schedule(
                        name = template.templateName,
                        phases = template.pollcy.mapNotNull { it.toSchedulePhase(modes) },
                        templateID = templateID
                    )

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex.localizedMessage ?: "Unknown error"))
                }
            }
        }
    }

    fun addTimePeriod() {
        val schedule = EditScheduleStore.shared.scheduleStream.value ?: return
        EditScheduleStore.shared.scheduleStream.value = SchedulePhaseHelper.addNewTimePeriod(schedule, modes = modes, device = config.currentDevice.value)
    }

    fun autoFillScheduleGaps() {
        val schedule = EditScheduleStore.shared.scheduleStream.value ?: return
        val mode = modes.firstOrNull() ?: return

        EditScheduleStore.shared.scheduleStream.value = SchedulePhaseHelper.appendPhasesInGaps(schedule, mode = mode, device = config.currentDevice.value)
    }

    fun delete(context: Context) {
        viewModelScope.launch {
            runCatching {
                uiState.value = UiLoadState(LoadState.Active("Deleting..."))

                try {
                    network.deleteScheduleTemplate(templateID)

                    uiState.value = UiLoadState(LoadState.Inactive)
                    shouldPopNavOnDismissal = true
                    alertDialogMessage.value = context.getString(R.string.battery_charge_schedule_was_saved)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex.localizedMessage ?: "Unknown error"))
                }
            }
        }
    }

    override fun resetDialogMessage() {
        alertDialogMessage.value = null

        if (shouldPopNavOnDismissal) {
            navController.popBackStack()
        }
        shouldPopNavOnDismissal = false
    }
}