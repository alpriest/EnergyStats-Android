package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoFoxESSNetworking
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

class ScheduleSummaryView(
    private val configManager: ConfigManaging,
    private val network: FoxESSNetworking,
    private val navController: NavHostController,
    private val userManager: UserManaging
) {
    @Composable
    fun Content(viewModel: ScheduleSummaryViewModel = viewModel(factory = ScheduleSummaryViewModelFactory(network, configManager, navController))) {
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
            is LoadState.Inactive ->
                SettingsPage {
                    SettingsColumnWithChild {
                        schedule?.let {
                            ScheduleView(it)
                        }
                    }
                }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleSummaryViewPreview() {
    EnergyStatsTheme {
        ScheduleSummaryView(
            configManager = FakeConfigManager(),
            network = DemoFoxESSNetworking(),
            navController = NavHostController(LocalContext.current),
            userManager = FakeUserManager()
        ).Content()
    }
}
