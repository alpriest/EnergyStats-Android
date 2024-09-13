package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.SettingsBottomSpace
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleView
import com.alpriest.energystats.ui.settings.inverter.schedule.asSchedule
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

class ScheduleTemplateListViewModelFactory(
    private val configManager: ConfigManaging,
    private val templateStore: TemplateStoring,
    private val navController: NavHostController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ScheduleTemplateListViewModel(configManager, templateStore, navController) as T
    }
}

class ScheduleTemplateListView(
    private val configManager: ConfigManaging,
    private val templateStore: TemplateStoring,
    private val navController: NavHostController,
    private val userManager: UserManaging
) {
    @Composable
    fun Content(
        viewModel: ScheduleTemplateListViewModel = viewModel(factory = ScheduleTemplateListViewModelFactory(configManager, templateStore, navController)),
        modifier: Modifier
    ) {
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
            is LoadState.Inactive -> {
                Loaded(templates, viewModel, modifier)
            }
        }
    }

    @Composable
    fun Loaded(templates: List<ScheduleTemplate>, viewModel: ScheduleTemplateListViewModel, modifier: Modifier) {
        SettingsPage(modifier) {
            templates.forEach {
                SettingsColumn {
                    Text(
                        text = it.name,
                        style = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSecondary),
                        modifier = Modifier
                            .padding(PaddingValues(top = 10.dp, bottom = 8.dp))
                            .fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = { viewModel.edit(it) },
                        border = null,
                        contentPadding = PaddingValues(),
                        shape = RectangleShape
                    ) {
                        ScheduleView(it.asSchedule(), modifier = Modifier.weight(1.0f))

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Edit"
                        )
                    }
                }
            }

            SettingsBottomSpace()
        }

        CreateTemplateView(viewModel)
    }
}

@Composable
fun CreateTemplateView(viewModel: ScheduleTemplateListViewModel) {
    val context = LocalContext.current
    val presentCreateAlert = remember { mutableStateOf(false) }

    Button(
        onClick = { presentCreateAlert.value = true }
    ) {
        Text(
            stringResource(id = R.string.create_new_template),
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }

    if (presentCreateAlert.value) {
        TemplateNameAlertDialog(configuration = AlertConfiguration.CreateTemplate) {
            presentCreateAlert.value = false
            it?.let {
                viewModel.createTemplate(it, context)
            }
        }
    }
}

@Preview(heightDp = 600, widthDp = 400)
@Composable
fun EditPhaseViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        ScheduleTemplateListView(
            configManager = FakeConfigManager(),
            templateStore = PreviewTemplateStore(),
            navController = NavHostController(LocalContext.current),
            userManager = FakeUserManager()
        ).Loaded(
            templates = listOf(
                ScheduleTemplate("1", "Summer saving", listOf()),
                ScheduleTemplate("2", "Winter overnight charge", listOf())
            ),
            viewModel = ScheduleTemplateListViewModel(
                FakeConfigManager(),
                PreviewTemplateStore(),
                NavHostController(LocalContext.current)
            ),
            Modifier
        )
    }
}
