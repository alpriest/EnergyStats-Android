package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.SettingsCheckbox
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsNavButton
import com.alpriest.energystats.ui.settings.SettingsPaddingValues
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.SettingsScreen
import com.alpriest.energystats.ui.settings.SettingsTitleView
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.PreviewTemplateStore
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.TemplateStoring
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

class ScheduleSummaryView(
    private val configManager: ConfigManaging,
    private val network: Networking,
    private val navController: NavHostController,
    private val userManager: UserManaging,
    private val templateStore: TemplateStoring
) {
    @Composable
    fun Content(viewModel: ScheduleSummaryViewModel = viewModel(factory = ScheduleSummaryViewModelFactory(network, configManager, navController, templateStore))) {
        val context = LocalContext.current
        val schedule = viewModel.scheduleStream.collectAsState().value
        val loadState = viewModel.uiState.collectAsState().value.state
        val supportedError = viewModel.supportedErrorStream.collectAsState().value

        MonitorAlertDialog(viewModel, userManager)

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
                        SettingsColumnWithChild(padding = SettingsPaddingValues.withVertical()) {
                            SettingsTitleView(stringResource(R.string.unsupported))
                            Text(
                                supportedError,
                                color = colors.onSecondary
                            )
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
        val schedulerEnabled = viewModel.schedulerEnabledStream.collectAsState().value
        val schedulerEnabledState = rememberSaveable { mutableStateOf(schedulerEnabled) }

        SettingsPage {
            SettingsColumn {
                SettingsCheckbox(
                    title = stringResource(R.string.enable_scheduler),
                    state = schedulerEnabledState,
                    onUpdate = {
                        viewModel.setSchedulerFlag(context, schedulerEnabledState.value)
                    }
                )
            }

            SettingsColumn(
                padding = SettingsPaddingValues.withVertical(),
                header = stringResource(R.string.schedule)
            ) {
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

            templates.forEach {
                SettingsColumn(
                    header = if (it == templates.first()) { stringResource(R.string.templates) } else { null }
                ) {
                    Text(
                        text = it.name,
                        style = TextStyle.Default.copy(color = colors.onSecondary),
                        modifier = Modifier
                            .padding(PaddingValues(top = 10.dp, bottom = 8.dp))
                            .fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = { viewModel.editTemplate(it) },
                        border = null,
                        contentPadding = PaddingValues()
                    ) {
                        ScheduleView(it.asSchedule(), modifier = Modifier.weight(1.0f))

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Edit"
                        )
                    }

                    ActivateButton {
                        viewModel.activate(it, context)
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

fun ScheduleTemplate.asSchedule(): Schedule {
    return Schedule(
        name = name,
        phases = phases,
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun ScheduleSummaryViewPreview() {
    val viewModel = ScheduleSummaryViewModel(DemoNetworking(), FakeConfigManager(), NavHostController(LocalContext.current), PreviewTemplateStore())
    val context = LocalContext.current
    LaunchedEffect(null) { viewModel.load(context) }

    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        ScheduleSummaryView(
            configManager = FakeConfigManager(),
            network = DemoNetworking(),
            navController = NavHostController(LocalContext.current),
            userManager = FakeUserManager(),
            templateStore = PreviewTemplateStore()
        ).Loaded(
            Schedule.preview(),
            viewModel
        )
    }
}
