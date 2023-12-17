package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsNavButton
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
            is LoadState.Inactive -> schedule?.let { Loaded(it, viewModel) }
        }
    }

    @Composable
    fun Loaded(schedule: Schedule, viewModel: ScheduleSummaryViewModel) {
        val templates = viewModel.templateStream.collectAsState().value
        val context = LocalContext.current

        SettingsPage {
            if (schedule.phases.isEmpty()) {
                NoScheduleView(viewModel)
            } else {
                Text(
                    text = "Current schedule",
                    style = MaterialTheme.typography.h4,
                    color = colors.onSecondary,
                    modifier = Modifier.fillMaxWidth()
                )

                SettingsColumnWithChild(padding = PaddingValues(start = 10.dp, top = 10.dp, bottom = 10.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.editSchedule() },
                        border = null,
                        contentPadding = PaddingValues()
                    ) {
                        ScheduleView(schedule, modifier = Modifier.weight(1.0f))

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Edit"
                        )
                    }
                }

                Text(
                    text = "Templates",
                    style = MaterialTheme.typography.h4,
                    color = colors.onSecondary,
                    modifier = Modifier.fillMaxWidth()
                )

                SettingsColumnWithChild {
                    templates.forEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(it.name)

                            Spacer(modifier = Modifier.weight(0.1f))

                            ActivateButton {
                                viewModel.activate(it, context)
                            }
                        }

                        if (templates.last() != it) {
                            Divider()
                        }
                    }
                }

                Button(onClick = { navController.navigate(ScheduleScreen.TemplateList.name) }) {
                    Text("Manage templates", color = colors.onPrimary)
                }
            }
        }
    }

    @Composable
    fun ActivateButton(onClick: () -> Unit) {
        Button(onClick = onClick) {
            Text(
                "Activate",
                color = colors.onPrimary
            )
        }
    }

    @Composable
    fun NoScheduleView(viewModel: ScheduleSummaryViewModel) {
        Column {
            Text(
                "You don't have a schedule defined.",
                modifier = Modifier.padding(vertical = 44.dp)
            )

            SettingsNavButton("Create a schedule") {
                viewModel.createSchedule()
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun ScheduleSummaryViewPreview() {
    val viewModel = ScheduleSummaryViewModel(DemoFoxESSNetworking(), FakeConfigManager(), NavHostController(LocalContext.current))
    val context = LocalContext.current
    LaunchedEffect(null) { viewModel.load(context) }

    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        ScheduleSummaryView(
            configManager = FakeConfigManager(),
            network = DemoFoxESSNetworking(),
            navController = NavHostController(LocalContext.current),
            userManager = FakeUserManager()
        ).Content(
            viewModel
        )
    }
}
