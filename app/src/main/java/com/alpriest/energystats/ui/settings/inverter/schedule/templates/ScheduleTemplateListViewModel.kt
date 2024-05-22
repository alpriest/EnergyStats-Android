package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialogData
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.settings.SettingsScreen
import com.alpriest.energystats.ui.settings.inverter.schedule.EditScheduleStore
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
            templateStream.value = templateStore.load()
        }
    }

    fun createTemplate(templateName: String, context: Context) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        templateStore.create(templateName)
        load(context)
    }

    fun edit(template: ScheduleTemplate) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        EditScheduleStore.shared.reset()
        EditScheduleStore.shared.templateStream.value = template
        EditScheduleStore.shared.allowDeletion = true

        navController.navigate(SettingsScreen.EditTemplate.name)
    }
}