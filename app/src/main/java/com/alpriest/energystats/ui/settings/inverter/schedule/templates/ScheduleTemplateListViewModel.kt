package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.settings.SettingsScreen
import com.alpriest.energystats.ui.settings.inverter.schedule.EditScheduleStore
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplateSummary
import com.alpriest.energystats.ui.settings.inverter.schedule.toSchedulePhase
import com.alpriest.energystats.ui.settings.inverter.schedule.toScheduleTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ScheduleTemplateListViewModel(
    private val config: ConfigManaging,
    private val network: FoxESSNetworking,
    private val navController: NavHostController
) : ViewModel(), AlertDialogMessageProviding {
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    val uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    val templateStream = MutableStateFlow<List<ScheduleTemplateSummary>>(listOf())

    suspend fun load(context: Context) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        runCatching {
            config.currentDevice.value?.let { device ->
                uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

                try {
                    val templatesResponse = network.fetchScheduleTemplates()
                    templateStream.value = templatesResponse.data.mapNotNull { it.toScheduleTemplate() }

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: "Unknown error"))
                }
            }
        }
    }

    suspend fun createTemplate(templateName: String, templateDescription: String, context: Context) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.saving)))

        try {
            network.createScheduleTemplate(templateName, templateDescription)
            uiState.value = UiLoadState(LoadState.Inactive)

            load(context)
        } catch (ex: Exception) {
            uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: "Unknown error"))
        }
    }

    fun edit(templateSummary: ScheduleTemplateSummary, context: Context) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        viewModelScope.launch {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

                try {
                    val template = network.fetchScheduleTemplate(deviceSN, templateSummary.id)
                    val modes = EditScheduleStore.shared.modes

                    EditScheduleStore.shared.scheduleStream.value = Schedule(
                        name = template.templateName,
                        phases = template.pollcy.mapNotNull { it.toSchedulePhase(modes) },
                        templateID = templateSummary.id,
                        description = template.content
                    )
                    navController.navigate(SettingsScreen.EditTemplate.name)

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: "Unknown error"))
                }
            }
        }
    }
}