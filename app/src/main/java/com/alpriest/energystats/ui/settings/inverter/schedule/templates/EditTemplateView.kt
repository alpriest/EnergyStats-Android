package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.SettingsTitleView
import com.alpriest.energystats.ui.settings.inverter.schedule.Schedule
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleDetailView

class EditTemplateView(
    private val configManager: ConfigManaging,
    private val network: FoxESSNetworking,
    private val navController: NavHostController,
    private val userManager: UserManaging
) {
    @Composable
    fun Content(viewModel: EditTemplateViewModel = viewModel(factory = EditTemplateViewModelFactory(configManager, network, navController))) {
        val schedule = viewModel.scheduleStream.collectAsState().value
        val loadState = viewModel.uiState.collectAsState().value.state
        val context = LocalContext.current

        MonitorAlertDialog(viewModel)

        LaunchedEffect(null) {
            viewModel.load(context)
        }

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState.value)
            is LoadState.Error -> ErrorView(loadState.reason, onRetry = { viewModel.load(context) }, onLogout = { userManager.logout() })
            is LoadState.Inactive -> schedule?.let { Loaded(it, viewModel, navController) }
        }
    }

    @Composable
    fun Loaded(schedule: Schedule, viewModel: EditTemplateViewModel, navController: NavHostController) {
        val context = LocalContext.current

        SettingsPage {
            SettingsTitleView("Edit template")
            ScheduleDetailView(viewModel.navController, schedule)

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { viewModel.addTimePeriod() }) {
                    Text("Add time period")
                }

                Button(onClick = { viewModel.autoFillScheduleGaps() }) {
                    Text("Autofill gaps")
                }

                Button(onClick = { viewModel.saveTemplate(context) }) {
                    Text("Save template")
                }

                Button(onClick = { viewModel.activate(context) }) {
                    Text("Activate template")
                }

                Button(onClick = { viewModel.delete(context) }) {
                    Text("Delete template")
                }
            }
        }
    }
}
