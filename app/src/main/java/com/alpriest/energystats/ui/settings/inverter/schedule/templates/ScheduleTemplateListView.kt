package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Divider
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
import com.alpriest.energystats.ui.settings.SettingsScreen
import com.alpriest.energystats.ui.settings.inverter.schedule.EditScheduleStore
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplateSummary
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

class ScheduleTemplateListViewModelFactory(
    private val configManager: ConfigManaging,
    private val network: FoxESSNetworking,
    private val navController: NavHostController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ScheduleTemplateListViewModel(configManager, network, navController) as T
    }
}

class ScheduleTemplateListView(
    private val configManager: ConfigManaging,
    private val network: FoxESSNetworking,
    private val navController: NavHostController,
    private val userManager: UserManaging
) {
    @Composable
    fun Content(viewModel: ScheduleTemplateListViewModel = viewModel(factory = ScheduleTemplateListViewModelFactory(configManager, network, navController))) {
        val context = LocalContext.current
        val loadState = viewModel.uiState.collectAsState().value.state
        val templates = viewModel.templateStream.collectAsState().value

        MonitorAlertDialog(viewModel)

        LaunchedEffect(null) {
            viewModel.load(context)
        }

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState.value)
            is LoadState.Error -> ErrorView(loadState.reason, onRetry = { viewModel.load(context) }, onLogout = { userManager.logout() })
            is LoadState.Inactive -> Loaded(templates, viewModel)
        }
    }

    @Composable
    fun Loaded(templates: List<ScheduleTemplateSummary>, viewModel: ScheduleTemplateListViewModel) {
        SettingsPage {
            Text(
                text = "Templates",
                style = MaterialTheme.typography.h4,
                color = MaterialTheme.colors.onSecondary,
                modifier = Modifier.fillMaxWidth()
            )

            SettingsColumnWithChild {
                templates.forEach {
                    OutlinedButton(
                        onClick = {
                            EditScheduleStore.shared.reset()
                            EditScheduleStore.shared.templateID = it.id
                            navController.navigate(SettingsScreen.EditTemplate.name)
                        },
                        border = null,
                        contentPadding = PaddingValues()
                    ) {
                        Text(it.name)

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
                triggerCreateFunction = false // Reset the trigger
            }
        }

        Text(
            text = "New template",
            style = MaterialTheme.typography.h4,
            color = MaterialTheme.colors.onSecondary,
            modifier = Modifier.fillMaxWidth()
        )

        SettingsColumnWithChild(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newTemplateName,
                onValueChange = { newTemplateName = it },
                label = { androidx.compose.material.Text(stringResource(R.string.username)) }
            )
            OutlinedTextField(
                value = newTemplateDescription,
                onValueChange = { newTemplateDescription = it },
                label = { Text("Description") }
            )

            Button(onClick = { triggerCreateFunction = true }) {
                Text(
                    "Create new template",
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
            network = DemoFoxESSNetworking(),
            navController = NavHostController(LocalContext.current),
            userManager = FakeUserManager()
        ).Loaded(
            templates = listOf(
                ScheduleTemplateSummary("1", "Summer saving", false),
                ScheduleTemplateSummary("2", "Winter overnight charge", false)
            ),
            viewModel = ScheduleTemplateListViewModel(
                FakeConfigManager(),
                DemoFoxESSNetworking(),
                NavHostController(LocalContext.current)
            )
        )
    }
}
