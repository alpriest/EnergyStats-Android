package com.alpriest.energystats.ui.settings.inverter.schedule.templates

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
import com.alpriest.energystats.ui.settings.inverter.schedule.EditScheduleStore
import com.alpriest.energystats.ui.settings.inverter.schedule.SchedulePhaseHelper
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.alpriest.energystats.ui.settings.inverter.schedule.WorkMode
import com.alpriest.energystats.ui.settings.inverter.schedule.asSchedule
import com.alpriest.energystats.ui.settings.inverter.schedule.errorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class EditTemplateViewModelFactory(
    private val configManager: ConfigManaging,
    private val network: Networking,
    private val navController: NavHostController,
    private val templateStore: TemplateStoring
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditTemplateViewModel(configManager, network, navController, templateStore) as T
    }
}

class EditTemplateViewModel(
    val configManager: ConfigManaging,
    val network: Networking,
    val navController: NavHostController,
    private val templateStore: TemplateStoring
) : ViewModel(), AlertDialogMessageProviding {
    val templateStream = EditScheduleStore.shared.templateStream
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    val uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    private val modes: List<WorkMode> = EditScheduleStore.modes(configManager)
    private var templateID: String = ""
    private var shouldPopNavOnDismissal = false

    fun load() {
        val sharedSchedule = EditScheduleStore.shared.scheduleStream.value
        val sharedTemplate = EditScheduleStore.shared.templateStream.value

        if (sharedSchedule == null && sharedTemplate != null) {
            templateID = sharedTemplate.id
            val template = templateStore.load().first { it.id == templateID }
            templateStream.value = template
            EditScheduleStore.shared.scheduleStream.value = template.asSchedule()
        } else if (sharedSchedule != null && sharedTemplate != null) {
            EditScheduleStore.shared.templateStream.value = ScheduleTemplate(templateID, sharedTemplate.name, sharedSchedule.phases)
        }

        uiState.value = UiLoadState(LoadState.Inactive)
    }

    fun addTimePeriod() {
        val template = templateStream.value ?: return
        val device = configManager.currentDevice.value ?: return
        val updatedSchedule = SchedulePhaseHelper.addNewTimePeriod(
            template.asSchedule(),
            modes,
            device,
            configManager.getDeviceSupports(DeviceCapability.ScheduleMaxSOC, device.deviceSN)
        )
        val updatedTemplate = ScheduleTemplate(templateID, template.name, updatedSchedule.phases)
        EditScheduleStore.shared.templateStream.value = updatedTemplate
    }

    fun autoFillScheduleGaps() {
        val template = templateStream.value ?: return
        val mode = modes.firstOrNull() ?: return
        val device = configManager.currentDevice.value ?: return
        val updatedSchedule = SchedulePhaseHelper.appendPhasesInGaps(
            template.asSchedule(),
            mode,
            device,
            configManager.getDeviceSupports(DeviceCapability.ScheduleMaxSOC, device.deviceSN)
        )
        val updatedTemplate = ScheduleTemplate(templateID, template.name, updatedSchedule.phases)
        EditScheduleStore.shared.templateStream.value = updatedTemplate
    }

    fun delete(context: Context) {
        val template = templateStream.value ?: return

        viewModelScope.launch {
            runCatching {
                templateStore.delete(template)

                uiState.value = UiLoadState(LoadState.Inactive)
                shouldPopNavOnDismissal = true
                EditScheduleStore.shared.reset()
                alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.your_template_was_deleted))
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
        val template = templateStream.value ?: return
        if (!template.asSchedule().isValid()) {
            alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.this_schedule_phase_contains_invalid_phases_please_correct_and_try_again))
            return
        }

        viewModelScope.launch {
            runCatching {
                templateStore.save(template)

                uiState.value = UiLoadState(LoadState.Inactive)
                shouldPopNavOnDismissal = true
                EditScheduleStore.shared.reset()
                alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.your_template_was_saved))
            }
        }
    }

    fun activate(context: Context) {
        val template = templateStream.value ?: return
        if (templateID == "") return

        viewModelScope.launch {
            runCatching {
                configManager.currentDevice.value?.let { device ->
                    val deviceSN = device.deviceSN

                    try {
                        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.saving)))
                        network.saveSchedule(deviceSN, template.asSchedule())

                        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.activating)))
                        network.setScheduleFlag(deviceSN, true)

                        uiState.value = UiLoadState(LoadState.Inactive)
                        shouldPopNavOnDismissal = true
                        EditScheduleStore.shared.reset()
                        alertDialogMessage.value = MonitorAlertDialogData(null, context.getString(R.string.your_template_was_activated))
                    } catch (ex: Exception) {
                        val message = errorMessage(ex, context)
                        uiState.value = UiLoadState(LoadState.Error(ex, message, allowRetry = false))
                    }
                }
            }
        }
    }

    fun duplicate(name: String) {
        val template = templateStream.value ?: return

        viewModelScope.launch {
            runCatching {
                templateStore.duplicate(template, name)

                EditScheduleStore.shared.reset()
                uiState.value = UiLoadState(LoadState.Inactive)
                navController.popBackStack()
            }
        }
    }

    fun rename(name: String) {
        val template = templateStream.value ?: return

        templateStore.rename(template, name)

        EditScheduleStore.shared.reset()
        uiState.value = UiLoadState(LoadState.Inactive)
        navController.popBackStack()
    }

    fun clearError() {
        uiState.value = UiLoadState(LoadState.Inactive)
    }
}