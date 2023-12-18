package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.flow.UiLoadState
import com.alpriest.energystats.ui.paramsgraph.AlertDialogMessageProviding
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplateSummary
import com.alpriest.energystats.ui.settings.inverter.schedule.toScheduleTemplate
import kotlinx.coroutines.flow.MutableStateFlow

class ScheduleTemplateListViewModel(
    private val config: ConfigManaging,
    private val network: FoxESSNetworking,
    private val navController: NavHostController
) : ViewModel(), AlertDialogMessageProviding {
    override val alertDialogMessage = MutableStateFlow<String?>(null)
    val uiState = MutableStateFlow(UiLoadState(LoadState.Inactive))
    val templateStream = MutableStateFlow<List<ScheduleTemplateSummary>>(listOf())

    suspend fun load(context: Context) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        runCatching {
            config.currentDevice.value?.let { device ->
                val deviceSN = device.deviceSN

                uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.loading)))

                try {
                    val scheduleResponse = network.fetchCurrentSchedule(deviceSN)
                    templateStream.value = scheduleResponse.data.mapNotNull { it.toScheduleTemplate() }

                    uiState.value = UiLoadState(LoadState.Inactive)
                } catch (ex: Exception) {
                    uiState.value = UiLoadState(LoadState.Error(ex.localizedMessage ?: "Unknown error"))
                }
            }
        }
    }

    suspend fun createTemplate(templateName: String, templateDescription: String, context: Context) {
        if (uiState.value.state != LoadState.Inactive) {
            return
        }

        runCatching {
            uiState.value = UiLoadState(LoadState.Active(context.getString(R.string.saving)))

            try {
                network.createScheduleTemplate(templateName, templateDescription)

                uiState.value = UiLoadState(LoadState.Inactive)
            } catch (ex: Exception) {
                uiState.value = UiLoadState(LoadState.Error(ex.localizedMessage ?: "Unknown error"))
            }
        }
    }
}