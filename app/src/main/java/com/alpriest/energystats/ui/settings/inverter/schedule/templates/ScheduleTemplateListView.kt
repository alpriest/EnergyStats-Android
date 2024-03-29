package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.SettingsTitleView
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplateSummary
import com.alpriest.energystats.ui.theme.DimmedTextColor
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

class ScheduleTemplateListViewModelFactory(
    private val configManager: ConfigManaging,
    private val network: Networking,
    private val navController: NavHostController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ScheduleTemplateListViewModel(configManager, network, navController) as T
    }
}

class ScheduleTemplateListView(
    private val configManager: ConfigManaging,
    private val network: Networking,
    private val navController: NavHostController,
    private val userManager: UserManaging
) {
    @Composable
    fun Content(viewModel: ScheduleTemplateListViewModel = viewModel(factory = ScheduleTemplateListViewModelFactory(configManager, network, navController))) {
        val context = LocalContext.current
        val loadState = viewModel.uiState.collectAsState().value.state
        val templates = viewModel.templateStream.collectAsState().value

        MonitorAlertDialog(viewModel, userManager)

        LaunchedEffect(null) {
            viewModel.load(context)
        }

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState.value)
            is LoadState.Error -> ErrorView(loadState.ex, loadState.reason, onRetry = { viewModel.load(context) }, onLogout = { userManager.logout() })
            is LoadState.Inactive -> Loaded(templates, viewModel)
        }
    }

    @Composable
    fun Loaded(templates: List<ScheduleTemplateSummary>, viewModel: ScheduleTemplateListViewModel) {
        val context = LocalContext.current

        SettingsPage {
            SettingsColumnWithChild {
                SettingsTitleView(stringResource(R.string.templates))

                templates.forEach {
                    OutlinedButton(
                        onClick = { viewModel.edit(it, context) },
                        border = null,
                        contentPadding = PaddingValues()
                    ) {
                        Text(
                            it.name,
                            color = colors.onSecondary
                        )

                        Spacer(modifier = Modifier.weight(0.1f))

                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Edit")
                    }

                    if (templates.last() != it) {
                        Divider()
                    }
                }
            }

            CreateTemplateView(viewModel)
        }
    }

    @Composable
    fun CreateTemplateView(viewModel: ScheduleTemplateListViewModel) {
        val context = LocalContext.current
        var newTemplateName by remember { mutableStateOf("") }
        var newTemplateDescription by remember { mutableStateOf("") }
        var triggerCreateFunction by remember { mutableStateOf(false) }

        LaunchedEffect(triggerCreateFunction) {
            if (triggerCreateFunction) {
                viewModel.createTemplate(newTemplateName, newTemplateDescription, context)
                triggerCreateFunction = false
            }
        }

        SettingsColumnWithChild(modifier = Modifier.fillMaxWidth()) {
            SettingsTitleView(stringResource(R.string.new_template))

            OutlinedTextField(
                value = newTemplateName,
                onValueChange = { newTemplateName = it },
                label = { Text(stringResource(R.string.name), color = DimmedTextColor) }
            )
            OutlinedTextField(
                value = newTemplateDescription,
                onValueChange = { newTemplateDescription = it },
                label = { Text(stringResource(R.string.description), color = DimmedTextColor) }
            )

            Button(onClick = { triggerCreateFunction = true }) {
                Text(
                    stringResource(R.string.create_new_template),
                    color = MaterialTheme.colors.onPrimary
                )
            }
        }
    }
}

@Preview(heightDp = 600, widthDp = 400)
@Composable
fun EditPhaseViewPreview() {
    EnergyStatsTheme {
        ScheduleTemplateListView(
            configManager = FakeConfigManager(),
            network = DemoNetworking(),
            navController = NavHostController(LocalContext.current),
            userManager = FakeUserManager()
        ).Loaded(
            templates = listOf(
                ScheduleTemplateSummary("1", "Summer saving", false),
                ScheduleTemplateSummary("2", "Winter overnight charge", false)
            ),
            viewModel = ScheduleTemplateListViewModel(
                FakeConfigManager(),
                DemoNetworking(),
                NavHostController(LocalContext.current)
            )
        )
    }
}
