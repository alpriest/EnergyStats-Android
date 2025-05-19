package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
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
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ButtonLabels
import com.alpriest.energystats.ui.settings.ContentWithBottomButtonPair
import com.alpriest.energystats.ui.settings.SettingsBottomSpace
import com.alpriest.energystats.ui.settings.SettingsPadding
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

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

        LaunchedEffect(null) {
            viewModel.load()
        }

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState.value)
            is LoadState.Error -> ErrorView(loadState.ex, loadState.reason, loadState.allowRetry, onRetry = { viewModel.load() }, onLogout = { userManager.logout() })
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
        navController = navController,
        onSave = { viewModel.saveSchedule(context) },
        { innerModifier ->
            SettingsPage(innerModifier) {
                ScheduleDetailView(viewModel.navController, schedule)

                Row(
                    modifier = Modifier.fillMaxWidth()
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

                SettingsBottomSpace()
            }
        },
        labels = ButtonLabels(context.getString(R.string.cancel), stringResource(id = R.string.save)),
        modifier = modifier
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 400)
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
