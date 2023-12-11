package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.glance.Button
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging

class EditScheduleView(
    private val configManager: ConfigManaging,
    private val network: FoxESSNetworking,
    private val userManager: UserManaging,
    private val navHostController: NavHostController
) {
    @Composable
    fun Content(viewModel: EditScheduleViewModel = viewModel(factory = EditScheduleViewModelFactory(network, configManager, navHostController))) {
        val context = LocalContext.current
        val schedule = viewModel.scheduleStream.collectAsState().value
        val loadState = viewModel.uiState.collectAsState().value.state

        MonitorAlertDialog(viewModel)

        LaunchedEffect(null) {
            viewModel.load(context)
        }

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState.value)
            is LoadState.Error -> ErrorView(loadState.reason, onRetry = { viewModel.load(context) }, onLogout = { userManager.logout() })
            is LoadState.Inactive -> schedule?.let { Loaded(it, viewModel) }
        }
    }

    @Composable
    fun Loaded(schedule: Schedule, viewModel: EditScheduleViewModel) {
        val context = LocalContext.current
        val allowDeletion = viewModel.allowDeletionStream.collectAsState().value

        ScheduleDetailView(schedule)

        Column {
            Button(text = "Add time period", onClick = { viewModel.addTimePeriod() })
            Button(text = "Autofill gaps", onClick = { viewModel.autoFillScheduleGaps() })
            Button(text = "Activate schedule", onClick = { viewModel.saveSchedule(context) })

            if (allowDeletion) {
                Button(text = "Delete schedule", onClick = { /* TODO */ })
            }
        }
    }
}
