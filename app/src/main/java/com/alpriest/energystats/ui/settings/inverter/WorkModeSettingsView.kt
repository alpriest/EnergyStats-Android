package com.alpriest.energystats.ui.settings.inverter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ContentWithBottomButtonPair
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.inverter.schedule.WorkMode
import com.alpriest.energystats.ui.settings.inverter.schedule.subtitle
import com.alpriest.energystats.ui.settings.inverter.schedule.title
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

class WorkModeSettingsView(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController,
    private val userManager: UserManaging
) {
    @Composable
    fun Content(viewModel: WorkModeViewModel = viewModel(factory = WorkModeViewModelFactory(network, configManager, navController))) {
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        val viewData = viewModel.viewDataStream.collectAsState().value
        val loadState = viewModel.uiState.collectAsState().value.state

        MonitorAlertDialog(viewModel, userManager)

        LaunchedEffect(null) {
            viewModel.load(context)
        }

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState)
            is LoadState.Error -> ErrorView(loadState.ex, loadState.reason, onRetry = { viewModel.load(context) }, onLogout = { userManager.logout() }, allowRetry = true)
            is LoadState.Inactive ->
                ContentWithBottomButtonPair(
                    navController,
                    onConfirm = { viewModel.save(context) },
                    dirtyStateFlow = viewModel.dirtyState,
                    content = { modifier ->
                        SettingsPage(modifier) {
                            SettingsColumnWithChild {
                                viewModel.items.forEachIndexed { index, workMode: WorkMode ->
                                    Column(
                                        modifier = Modifier
                                            .clickable { viewModel.select(workMode) }
                                            .padding(bottom = 24.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = viewData.workMode == workMode,
                                                onClick = {
                                                    viewModel.select(workMode)
                                                }
                                            )
                                            Text(
                                                workMode.title(context),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colorScheme.onSecondary
                                            )
                                        }

                                        workMode.subtitle(context)?.let {
                                            Row(modifier = Modifier.padding(start = 48.dp)) {
                                                Text(
                                                    it,
                                                    color = colorScheme.onSecondary
                                                )
                                            }
                                        }
                                    }

                                    if (index < viewModel.items.size - 1) {
                                        HorizontalDivider()
                                    }
                                }
                            }

                            FindOutMoreView(uriHandler, "https://github.com/TonyM1958/HA-FoxESS-Modbus/wiki/Inverter-Work-Modes")
                        }
                    })
        }
    }
}


@Preview(widthDp = 400)
@Composable
fun WorkModeViewPreview() {
    EnergyStatsTheme {
        WorkModeSettingsView(
            DemoNetworking(),
            FakeConfigManager(),
            NavHostController(LocalContext.current),
            FakeUserManager()
        ).Content()
    }
}
