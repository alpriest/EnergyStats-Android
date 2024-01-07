package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
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
import com.alpriest.energystats.ui.settings.SettingsScreen
import com.alpriest.energystats.ui.settings.SettingsTitleView
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
        val supportedError = viewModel.supportedErrorStream.collectAsState().value

        MonitorAlertDialog(viewModel)

        LaunchedEffect(null) {
            viewModel.load(context)
        }

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState.value)
            is LoadState.Error -> ErrorView(loadState.ex, loadState.reason, onRetry = { viewModel.load(context) }, onLogout = { userManager.logout() })
            is LoadState.Inactive -> {
                if (supportedError == null) {
                    schedule?.let { Loaded(it, viewModel) }
                } else {
                    SettingsPage {
                        SettingsColumnWithChild(padding = PaddingValues(start = 10.dp, top = 10.dp, bottom = 10.dp)) {
                            SettingsTitleView("Unsupported")
                            Text(supportedError)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Loaded(schedule: Schedule, viewModel: ScheduleSummaryViewModel) {
        val templates = viewModel.templateStream.collectAsState().value
        val context = LocalContext.current

        SettingsPage {
            SettingsColumnWithChild(padding = PaddingValues(start = 10.dp, top = 10.dp, bottom = 10.dp)) {
                SettingsTitleView(stringResource(R.string.active_schedule))
                Spacer(modifier = Modifier.height(16.dp))

                if (schedule.phases.isEmpty()) {
                    NoScheduleView(viewModel)
                } else {
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
            }

            SettingsColumnWithChild {
                SettingsTitleView(stringResource(R.string.templates))

                if (schedule.phases.isEmpty()) {
                    Text(
                        stringResource(R.string.you_have_no_templates),
                        color = colors.onSecondary
                    )
                } else {
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
            }

            Button(onClick = { navController.navigate(SettingsScreen.TemplateList.name) }) {
                Text(stringResource(R.string.manage_templates), color = colors.onPrimary)
            }
        }
    }

    @Composable
    fun ActivateButton(onClick: () -> Unit) {
        Button(onClick = onClick) {
            Text(
                stringResource(R.string.activate),
                color = colors.onPrimary
            )
        }
    }

    @Composable
    fun NoScheduleView(viewModel: ScheduleSummaryViewModel) {
        Column {
            SettingsNavButton(stringResource(R.string.create_a_schedule)) {
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
