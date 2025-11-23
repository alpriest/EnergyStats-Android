package com.alpriest.energystats.ui.settings.inverter.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.DeviceCapability
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
    val configManager: ConfigManaging,
    val network: Networking,
    val navController: NavHostController
) : ViewModel(), AlertDialogMessageProviding {
    val scheduleStream = EditScheduleStore.shared.scheduleStream
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    val uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    private val modes: List<WorkMode> = EditScheduleStore.modes(configManager)
    private var shouldPopNavOnDismissal = false

    private val _dirtyState = MutableStateFlow(false)
    val dirtyState: StateFlow<Boolean> = _dirtyState

    private var originalValue: Schedule? = null

    init {
        originalValue = scheduleStream.value

        viewModelScope.launch {
            scheduleStream.collect {
                _dirtyState.value = originalValue != it
            }
        }
    }

    suspend fun saveSchedule(context: Context) {
        val schedule = scheduleStream.value ?: return
        val deviceSN = configManager.currentDevice.value?.deviceSN ?: return
        if (!schedule.isValid()) {
            alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.battery_periods_overlap))
            return
        }

        runCatching {
            uiState.value = UiLoadState(LoadState.Active.Saving)
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
        val schedule = scheduleStream.value ?: return
        val device = configManager.currentDevice.value ?: return

        scheduleStream.value = SchedulePhaseHelper.addNewTimePeriod(
            schedule,
            modes,
            device,
            initialiseMaxSOC = configManager.getDeviceSupports(DeviceCapability.ScheduleMaxSOC, device.deviceSN)
        )
    }

    fun autoFillScheduleGaps() {
        val schedule = scheduleStream.value ?: return
        val mode = modes.firstOrNull() ?: return
        val device = configManager.currentDevice.value ?: return

        scheduleStream.value = SchedulePhaseHelper.appendPhasesInGaps(
            schedule,
            mode,
            device,
            initialiseMaxSOC = configManager.getDeviceSupports(DeviceCapability.ScheduleMaxSOC, device.deviceSN)
        )
    }

    override fun resetDialogMessage() {
        alertDialogMessage.value = null

        if (shouldPopNavOnDismissal) {
            navController.popBackStack()
        }
        shouldPopNavOnDismissal = false
    }
}
