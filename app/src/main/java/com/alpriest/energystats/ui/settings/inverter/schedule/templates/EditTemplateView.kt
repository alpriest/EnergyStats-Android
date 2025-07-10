package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.alpriest.energystats.ui.settings.ButtonLabels
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.ContentWithBottomButtonPair
import com.alpriest.energystats.ui.settings.SettingsBottomSpace
import com.alpriest.energystats.ui.settings.SettingsPadding
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleDetailView
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.alpriest.energystats.ui.settings.inverter.schedule.UnusedSchedulePeriodWarning
import com.alpriest.energystats.ui.settings.inverter.schedule.asSchedule
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.PaleWhite
import com.alpriest.energystats.ui.theme.PowerFlowNegative

class EditTemplateView(
    private val configManager: ConfigManaging,
    private val network: Networking,
    private val navController: NavHostController,
    private val userManager: UserManaging,
    private val templateStore: TemplateStoring
) {
    @Composable
    fun Content(viewModel: EditTemplateViewModel = viewModel(factory = EditTemplateViewModelFactory(configManager, network, navController, templateStore)), modifier: Modifier) {
        val template = viewModel.templateStream.collectAsState().value
        val loadState = viewModel.uiState.collectAsState().value.state

        MonitorAlertDialog(viewModel, userManager)

        LaunchedEffect(null) {
            viewModel.load()
        }

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState.value)
            is LoadState.Error -> ErrorView(
                loadState.ex,
                loadState.reason,
                loadState.allowRetry,
                onRetry = {
                    if (loadState.allowRetry) {
                        viewModel.load()
                    } else {
                        viewModel.clearError()
                    }
                },
                onLogout = { userManager.logout() }
            )
            is LoadState.Inactive -> template?.let { it ->
                Loaded(it, viewModel, modifier)
            }
        }
    }

    @Composable
    fun Loaded(template: ScheduleTemplate, viewModel: EditTemplateViewModel, modifier: Modifier) {
        val context = LocalContext.current
        val presentDuplicateAlert = remember { mutableStateOf(false) }
        val presentRenameAlert = remember { mutableStateOf(false) }

        ContentWithBottomButtonPair(
            navController = navController,
            onConfirm = { viewModel.saveTemplate(context) },
            { innerModifier ->
                SettingsPage(innerModifier) {
                    ScheduleDetailView(viewModel.navController, template.asSchedule())

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = SettingsPadding.PANEL_OUTER_HORIZONTAL)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ESButton(
                                onClick = { viewModel.activate(context) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Image(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Activate",
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                            }

                            ESButton(
                                onClick = { presentDuplicateAlert.value = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Image(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Duplicate",
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                            }

                            ESButton(
                                onClick = { presentRenameAlert.value = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Image(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Rename",
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                            }

                            ESButton(
                                onClick = { viewModel.delete(context) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PowerFlowNegative,
                                    contentColor = PaleWhite
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Image(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                            }
                        }

                        if (template.phases.isNotEmpty()) {
                            Text(stringResource(R.string.time_period_missing_warning))

                            UnusedSchedulePeriodWarning(template.asSchedule())
                        }
                    }

                    SettingsBottomSpace()
                }

                if (presentDuplicateAlert.value) {
                    TemplateNameAlertDialog(AlertConfiguration.DuplicateTemplate) {
                        presentDuplicateAlert.value = false
                        it?.let {
                            viewModel.duplicate(it)
                        }
                    }
                }

                if (presentRenameAlert.value) {
                    TemplateNameAlertDialog(AlertConfiguration.RenameTemplate) {
                        presentRenameAlert.value = false
                        it?.let {
                            viewModel.rename(it)
                        }
                    }
                }
            },
            labels = ButtonLabels(context.getString(R.string.cancel), stringResource(id = R.string.save)),
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditTemplateViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        EditTemplateView(
            configManager = FakeConfigManager(),
            network = DemoNetworking(),
            navController = NavHostController(LocalContext.current),
            userManager = FakeUserManager(),
            templateStore = PreviewTemplateStore()
        ).Loaded(
            template = ScheduleTemplate(
                id = "123",
                name = "Winter routine",
                phases = listOf()
            ),
            viewModel = EditTemplateViewModel(
                FakeConfigManager(),
                DemoNetworking(),
                NavHostController(LocalContext.current),
                PreviewTemplateStore()
            ),
            Modifier
        )
    }
}
