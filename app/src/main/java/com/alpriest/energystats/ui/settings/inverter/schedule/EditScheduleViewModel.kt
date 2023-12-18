package com.alpriest.energystats.ui.settings.inverter.schedule

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class EditScheduleViewModelFactory(
    private val configManager: ConfigManaging,
    private val network: FoxESSNetworking,
    private val navController: NavHostController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditScheduleViewModel(configManager, network, navController) as T
    }
}

class EditScheduleViewModel(
    val config: ConfigManaging,
    val network: FoxESSNetworking,
    val navController: NavHostController
) : ViewModel(), AlertDialogMessageProviding {
    val scheduleStream = EditScheduleStore.shared.scheduleStream
    override val alertDialogMessage = MutableStateFlow<String?>(null)
    val uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    val allowDeletionStream = MutableStateFlow(false)
    private var modes: List<SchedulerModeResponse> = listOf()
    private var shouldPopNavOnDismissal = false

    fun load() {
        allowDeletionStream.value = EditScheduleStore.shared.allowDeletion
        modes = EditScheduleStore.shared.modes
    }

    suspend fun saveSchedule(context: Context) {
        val schedule = EditScheduleStore.shared.scheduleStream.value ?: return
        val deviceSN = config.currentDevice.value?.deviceSN ?: return
        if (!schedule.isValid()) {
            alertDialogMessage.value = "overlapping_time_periods"
            return
        }

        runCatching {
            uiState.value = UiLoadState(LoadState.Active("Activating..."))
            try {
                network.saveSchedule(deviceSN = deviceSN, schedule = schedule)

                shouldPopNavOnDismissal = true
                uiState.value = UiLoadState(LoadState.Inactive)
                alertDialogMessage.value = context.getString(R.string.inverter_charge_schedule_settings_saved)
            } catch (ex: Exception) {
                uiState.value = UiLoadState(LoadState.Error(ex.localizedMessage ?: "Unknown error"))
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
                config.currentDevice.value?.let { device ->
                    val deviceSN = device.deviceSN

                    uiState.value = UiLoadState(LoadState.Active("Deleting..."))

                    try {
                        network.deleteSchedule(deviceSN)

                        uiState.value = UiLoadState(LoadState.Inactive)
                        shouldPopNavOnDismissal = true
                        alertDialogMessage.value = "Your schedule was deleted"
                    } catch (ex: Exception) {
                        uiState.value = UiLoadState(LoadState.Error(ex.localizedMessage ?: "Unknown error"))
                    }
                } ?: {
                    uiState.value = UiLoadState(LoadState.Inactive)
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
