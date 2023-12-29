package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoFoxESSNetworking
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ButtonLabels
import com.alpriest.energystats.ui.settings.ContentWithBottomButtons
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.SettingsTitleView
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

class EditScheduleView(
    private val configManager: ConfigManaging,
    private val network: FoxESSNetworking,
    private val navController: NavHostController,
    private val userManager: UserManaging
) {
    @Composable
    fun Content(viewModel: EditScheduleViewModel = viewModel(factory = EditScheduleViewModelFactory(configManager, network, navController))) {
        val schedule = viewModel.scheduleStream.collectAsState().value
        val loadState = viewModel.uiState.collectAsState().value.state

        MonitorAlertDialog(viewModel)

        LaunchedEffect(null) {
            viewModel.load()
        }

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState.value)
            is LoadState.Error -> ErrorView(loadState.ex, loadState.reason, onRetry = { viewModel.load() }, onLogout = { userManager.logout() })
            is LoadState.Inactive -> schedule?.let { Loaded(it, viewModel, navController) }
        }
    }
}

@Composable
fun Loaded(schedule: Schedule, viewModel: EditScheduleViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val allowDeletion = viewModel.allowDeletionStream.collectAsState().value

    ContentWithBottomButtons(
        navController = navController,
        onSave = { viewModel.saveSchedule(context) },
        { modifier ->
            SettingsPage(modifier) {
                ScheduleDetailView(stringResource(R.string.edit_schedule), viewModel.navController, schedule)

                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { viewModel.addTimePeriod() }) {
                        Text(stringResource(R.string.add_time_period))
                    }
                    Button(onClick = { viewModel.autoFillScheduleGaps() }) {
                        Text(stringResource(R.string.autofill_gaps))
                    }

                    if (allowDeletion) {
                        Button(onClick = { viewModel.delete(context) }) {
                            Text(stringResource(R.string.delete_schedule))
                        }
                    }
                }
            }
        },
        labels = ButtonLabels(context.getString(R.string.cancel), stringResource(id = R.string.activate))
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun EditScheduleViewPreview() {
    EnergyStatsTheme {
        Loaded(
            schedule = Schedule.preview(),
            viewModel = EditScheduleViewModel(
                FakeConfigManager(),
                DemoFoxESSNetworking(),
                NavHostController(LocalContext.current)
            ),
            navController = NavHostController(LocalContext.current)
        )
    }
}
