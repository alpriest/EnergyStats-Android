package com.alpriest.energystats.ui.settings.inverter.schedule

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.alpriest.energystats.models.SchedulerModeResponse
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class EditScheduleStore(
    var scheduleStream: MutableStateFlow<Schedule?> = MutableStateFlow(null),
    var phaseId: String? = null,
    var allowDeletion: Boolean = false,
    var modes: List<SchedulerModeResponse> = listOf()
) {
    companion object {
        val shared: EditScheduleStore = EditScheduleStore()
    }
}

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
    override val alertDialogMessage = MutableStateFlow<String?>(null)
    val uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    val scheduleStream = MutableStateFlow<Schedule?>(null)
    val allowDeletionStream = MutableStateFlow(false)
    private var modes: List<SchedulerModeResponse> = listOf()

    fun load(context: Context) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        viewModelScope.launch {
            EditScheduleStore.shared.scheduleStream.collect {
                scheduleStream.value = it
                allowDeletionStream.value = EditScheduleStore.shared.allowDeletion
                modes = EditScheduleStore.shared.modes
            }
        }
    }

    fun saveSchedule(context: Context) {
        viewModelScope.launch {
            navController.popBackStack()
        }
    }

    fun addTimePeriod() {
        val schedule = scheduleStream.value ?: return
        scheduleStream.value = SchedulePhaseHelper.addNewTimePeriod(schedule, modes = modes, device = config.currentDevice.value)
    }

    fun autoFillScheduleGaps() {
        val schedule = scheduleStream.value ?: return
        val mode = modes.firstOrNull() ?: return

        scheduleStream.value = SchedulePhaseHelper.appendPhasesInGaps(schedule, mode = mode, device = config.currentDevice.value)
    }
}
