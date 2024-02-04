package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.settings.inverter.schedule.EditScheduleStore
import com.alpriest.energystats.ui.settings.inverter.schedule.SchedulePhaseHelper
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
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    val uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    private var modes = EditScheduleStore.shared.modes
    private var templateID: String = ""
    private var shouldPopNavOnDismissal = false

    fun load() {
        templateID = EditScheduleStore.shared.scheduleStream.value?.templateID ?: return
    }

    fun addTimePeriod() {
        val schedule = scheduleStream.value ?: return
        EditScheduleStore.shared.scheduleStream.value = SchedulePhaseHelper.addNewTimePeriod(schedule, modes = modes, device = config.currentDevice.value)
    }

    fun autoFillScheduleGaps() {
        val schedule = scheduleStream.value ?: return
        val mode = modes.firstOrNull() ?: return

        EditScheduleStore.shared.scheduleStream.value = SchedulePhaseHelper.appendPhasesInGaps(schedule, mode = mode, device = config.currentDevice.value)
    }

    fun delete(context: Context) {
        viewModelScope.launch {
            runCatching {
                uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.deleting)))

                // TODO
//                try {
//                    network.deleteScheduleTemplate(templateID)
//
//                    uiState.value = UiLoadState(LoadState.Inactive)
//                    shouldPopNavOnDismissal = true
//                    EditScheduleStore.shared.reset()
//                    alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.your_template_was_deleted))
//                } catch (ex: Exception) {
//                    uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: "Unknown error"))
//                }
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

    fun saveTemplate(context: Context) {
        val schedule = scheduleStream.value ?: return
        if (templateID == "") return

        viewModelScope.launch {
            runCatching {
                config.currentDevice.value?.let { device ->
                    val deviceSN = device.deviceSN
                    uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.saving)))

                    // TODO
//                    try {
//                        network.saveScheduleTemplate(deviceSN, ScheduleTemplate(templateID, schedule.phases))
//
//                        uiState.value = UiLoadState(LoadState.Inactive)
//                        shouldPopNavOnDismissal = true
//                        EditScheduleStore.shared.reset()
//                        alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.your_template_was_saved))
//                    } catch (ex: Exception) {
//                        uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: "Unknown error"))
//                    }
                }
            }
        }
    }

    fun activate(context: Context) {
        val schedule = scheduleStream.value ?: return
        if (templateID == "") return

        viewModelScope.launch {
            runCatching {
                config.currentDevice.value?.let { device ->
                    val deviceSN = device.deviceSN

                    // TODO
//                    try {
//                        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.saving)))
//                        network.saveScheduleTemplate(deviceSN, ScheduleTemplate(templateID, schedule.phases))
//
//                        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.activating)))
//                        network.enableScheduleTemplate(deviceSN, templateID)
//
//                        uiState.value = UiLoadState(LoadState.Inactive)
//                        shouldPopNavOnDismissal = true
//                        EditScheduleStore.shared.reset()
//                        alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.your_template_was_activated))
//                    } catch (ex: Exception) {
//                        uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: "Unknown error"))
//                    }
                }
            }
        }
    }
}