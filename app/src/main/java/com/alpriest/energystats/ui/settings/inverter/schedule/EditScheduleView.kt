package com.alpriest.energystats.ui.settings.inverter.schedule

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ContentWithBottomButtonPair
import com.alpriest.energystats.ui.settings.SettingsBottomSpace
import com.alpriest.energystats.ui.settings.SettingsPadding
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.shared.models.Schedule

class EditScheduleView(
    private val configManager: ConfigManaging,
    private val network: Networking,
    private val navController: NavHostController,
    private val userManager: UserManaging
) {
    @Composable
    fun Content(viewModel: EditScheduleViewModel = viewModel(factory = EditScheduleViewModelFactory(configManager, network, navController)), modifier: Modifier) {
        val schedule = viewModel.scheduleStream.collectAsState().value
        val loadState = viewModel.uiState.collectAsState().value.state
        trackScreenView("Edit Schedule", "EditScheduleView")

        MonitorAlertDialog(viewModel, userManager)

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState)
            is LoadState.Error -> ErrorView(loadState.ex, loadState.reason, false, onRetry = {}, onLogout = { userManager.logout() })
            is LoadState.Inactive -> schedule?.let {
                Loaded(schedule, viewModel, navController, Modifier)
            }
        }
    }
}

@Composable
private fun Loaded(schedule: Schedule, viewModel: EditScheduleViewModel, navController: NavHostController, modifier: Modifier) {
    val context = LocalContext.current

    ContentWithBottomButtonPair(
        navController,
        modifier = modifier,
        onConfirm = { viewModel.saveSchedule(context) },
        dirtyStateFlow = viewModel.dirtyState,
        content = { innerModifier ->
            SettingsPage(innerModifier) {
                ScheduleDetailView(viewModel.navController, schedule)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SettingsPadding.PANEL_INNER_HORIZONTAL),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ESButton(
                        onClick = { viewModel.addTimePeriod() },
                        modifier = Modifier.weight(1.0f)
                    ) {
                        Text(stringResource(R.string.add_time_period))
                    }
                    ESButton(
                        onClick = { viewModel.autoFillScheduleGaps() },
                        modifier = Modifier.weight(1.0f)
                    ) {
                        Text(stringResource(R.string.autofill_gaps))
                    }
                }

                Column(Modifier.padding(SettingsPadding.PANEL_INNER_HORIZONTAL)) {
                    Text(stringResource(R.string.time_period_missing_warning))

                    UnusedSchedulePeriodWarning(schedule)
                }

                SettingsBottomSpace()
            }
        }
    )
}

@Composable
fun UnusedSchedulePeriodWarning(schedule: Schedule) {
    if (schedule.hasTooManyPhases) {
        Row(modifier = Modifier.padding(top = 16.dp)) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(Color.White)
                    .diagonalLinesIf(true)
            ) {
            }

            Text(
                "Will not be used by FoxESS if this template is activated (max 8 periods).",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
fun EditScheduleViewPreview() {
    EnergyStatsTheme {
        Loaded(
            schedule = Schedule.preview(),
            viewModel = EditScheduleViewModel(
                FakeConfigManager(),
                DemoNetworking(),
                NavHostController(LocalContext.current)
            ),
            NavHostController(LocalContext.current),
            Modifier
        )
    }
}

