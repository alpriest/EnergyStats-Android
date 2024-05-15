package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import kotlinx.coroutines.flow.MutableStateFlow

class ScheduleTemplateListViewModel(
    private val config: ConfigManaging,
    private val templateStore: TemplateStoring,
    private val navController: NavHostController
) : ViewModel(), AlertDialogMessageProviding {
    override val alertDialogMessage = MutableStateFlow<MonitorAlertDialogData?>(null)
    val uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    val templateStream = MutableStateFlow<List<ScheduleTemplate>>(listOf())

    fun load(context: Context) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        config.currentDevice.value?.let { _ ->
            uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

            templateStream.value = templateStore.load()
            uiState.value = UiLoadState(LoadState.Inactive)
        }
    }

    suspend fun createTemplate(templateName: String, templateDescription: String, context: Context) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.saving)))

        // TODO
//        try {
//            network.createScheduleTemplate(templateName, templateDescription)
//            uiState.value = UiLoadState(LoadState.Inactive)
//
//            load(context)
//        } catch (ex: Exception) {
//            uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: "Unknown error"))
//        }
    }

    fun edit(templateSummary: ScheduleTemplate, context: Context) {
//        if (uiState.value.state != LoadState.Inactive) {
//            return
//        }
//
//        viewModelScope.launch {
//            config.currentDevice.value?.let { device ->
//                val deviceSN = device.deviceSN
//
//                uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))
//
//                try {
//                    // TODO
////                    val template = network.fetchScheduleTemplate(deviceSN, templateSummary.id)
//                    val modes = EditScheduleStore.shared.modes
//
////                    EditScheduleStore.shared.scheduleStream.value = Schedule(
////                        name = template.templateName,
////                        phases = template.pollcy.mapNotNull { it.toSchedulePhase(modes) },
////                        templateID = templateSummary.id,
////                        description = template.content
////                    )
//                    navController.navigate(SettingsScreen.EditTemplate.name)
//
//                    uiState.value = UiLoadState(LoadState.Inactive)
//                } catch (ex: Exception) {
//                    uiState.value = UiLoadState(LoadState.Error(ex, ex.localizedMessage ?: "Unknown error"))
//                }
//            }
//        }
    }
}