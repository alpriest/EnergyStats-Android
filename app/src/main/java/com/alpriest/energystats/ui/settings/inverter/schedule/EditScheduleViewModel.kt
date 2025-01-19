package com.alpriest.energystats.ui.settings.inverter.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import kotlinx.coroutines.flow.MutableStateFlow

class EditScheduleViewModelFactory(
    private val configManager: ConfigManaging,
    private val network: Networking,
    private val navController: NavHostController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditScheduleViewModel(configManager, network, navController) as T
    }
}

class EditScheduleViewModel(
    val config: ConfigManaging,
    val network: Networking,
    val navController: NavHostController
) : ViewModel(), AlertDialogMessageProviding {
    val scheduleStream = EditScheduleStore.shared.scheduleStream
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    val uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    val allowDeletionStream = MutableStateFlow(false)
    private var modes = EditScheduleStore.shared.modes
    private var shouldPopNavOnDismissal = false

    fun load() {
        allowDeletionStream.value = EditScheduleStore.shared.allowDeletion
    }

    suspend fun saveSchedule(context: Context) {
        val schedule = EditScheduleStore.shared.scheduleStream.value ?: return
        val deviceSN = config.currentDevice.value?.deviceSN ?: return
        if (!schedule.isValid()) {
            alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.battery_periods_overlap))
            return
        }

        runCatching {
            uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.saving)))
            try {
                network.saveSchedule(deviceSN = deviceSN, schedule = schedule)

                shouldPopNavOnDismissal = true
                alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.inverter_charge_schedule_settings_saved))
                EditScheduleStore.shared.reset()
                uiState.value = UiLoadState(LoadState.Inactive)
            } catch (ex: Exception) {
                val message = errorMessage(ex, context)
                uiState.value = UiLoadState(LoadState.Error(ex, message))
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

    override fun resetDialogMessage() {
        alertDialogMessage.value = null

        if (shouldPopNavOnDismissal) {
            navController.popBackStack()
        }
        shouldPopNavOnDismissal = false
    }
}
