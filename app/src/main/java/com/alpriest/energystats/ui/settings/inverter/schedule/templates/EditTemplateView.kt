package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleDetailView
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.alpriest.energystats.ui.settings.inverter.schedule.asSchedule
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
    fun Content(viewModel: EditTemplateViewModel = viewModel(factory = EditTemplateViewModelFactory(configManager, network, navController, templateStore))) {
        val template = viewModel.templateStream.collectAsState().value
        val loadState = viewModel.uiState.collectAsState().value.state

        MonitorAlertDialog(viewModel, userManager)

        LaunchedEffect(null) {
            viewModel.load()
        }

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState.value)
            is LoadState.Error -> ErrorView(loadState.ex, loadState.reason, onRetry = { viewModel.load() }, onLogout = { userManager.logout() })
            is LoadState.Inactive -> template?.let { Loaded(it, viewModel) }
        }
    }

    @Composable
    fun Loaded(template: ScheduleTemplate, viewModel: EditTemplateViewModel) {
        val context = LocalContext.current

        SettingsPage {
            ScheduleDetailView(stringResource(R.string.edit_template), viewModel.navController, template.asSchedule())

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { viewModel.addTimePeriod() }) {
                    Text(stringResource(R.string.add_time_period))
                }

                Button(onClick = { viewModel.autoFillScheduleGaps() }) {
                    Text(stringResource(R.string.autofill_gaps))
                }

                Button(onClick = { viewModel.saveTemplate(context) }) {
                    Text(stringResource(R.string.save_template))
                }

                Button(onClick = { viewModel.activate(context) }) {
                    Text(stringResource(R.string.activate_template))
                }

                CreateTemplateButtonView("Duplicate template") {
                    viewModel.duplicate(it)
                }

                Button(
                    onClick = { viewModel.delete(context) },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = PowerFlowNegative,
                        contentColor = PaleWhite
                    )
                ) {
                    Text(stringResource(R.string.delete_template))
                }
            }
        }
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
                name = "foo",
                phases = listOf()
            ),
            viewModel = EditTemplateViewModel(
                FakeConfigManager(),
                DemoNetworking(),
                NavHostController(LocalContext.current),
                PreviewTemplateStore()
            )
        )
    }
}
