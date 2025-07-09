package com.alpriest.energystats.ui.settings.inverter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
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
        val selectedWorkMode = viewModel.workModeStream.collectAsState().value
        val loadState = viewModel.uiState.collectAsState().value.state

        MonitorAlertDialog(viewModel, userManager)

        LaunchedEffect(null) {
            viewModel.load(context)
        }

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState.value)
            is LoadState.Error -> ErrorView(loadState.ex, loadState.reason, onRetry = { viewModel.load(context) }, onLogout = { userManager.logout() }, allowRetry = true)
            is LoadState.Inactive ->
                ContentWithBottomButtonPair(navController, onConfirm = { viewModel.save(context) }, { modifier ->
                    SettingsPage(modifier) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = Color.Red,
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(24.dp)
                            )
                            Text(
                                stringResource(R.string.only_change_these_values_if_you_know_what_you_are_doing),
                                color = colorScheme.onSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = Color.Red,
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(24.dp)
                            )
                        }

                        SettingsColumnWithChild {
                            WorkMode.entries.forEach { workMode ->
                                Row {
                                    Column(
                                        modifier = Modifier
                                            .clickable { viewModel.select(workMode) }
                                            .padding(bottom = 24.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = selectedWorkMode == workMode,
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

                                        Row(modifier = Modifier.padding(start = 48.dp)) {
                                            Text(
                                                workMode.subtitle(context),
                                                color = colorScheme.onSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        )
                        {
                            Button(
                                onClick = {
                                    uriHandler.openUri("https://github.com/TonyM1958/HA-FoxESS-Modbus/wiki/Inverter-Work-Modes")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    contentColor = colorScheme.primary,
                                    containerColor = Color.Transparent
                                ),
                                elevation = null,
                            ) {
                                Icon(
                                    Icons.Default.OpenInBrowser, contentDescription = "Open In Browser", modifier = Modifier.padding(end = 5.dp)
                                )
                                Text(
                                    stringResource(R.string.find_out_more_about_work_modes),
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }, Modifier)
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
