package com.alpriest.energystats.ui.settings.inverter.schedule.templates

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
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
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.helpers.ErrorView
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.SettingsBottomSpace
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsFooterView
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleView
import com.alpriest.energystats.ui.settings.inverter.schedule.asSchedule
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.OutlinedESButton

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
        trackScreenView("Templates", "ScheduleTemplateListView")

        MonitorAlertDialog(viewModel, userManager)

        LaunchedEffect(null) {
            viewModel.load(context)
        }

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState)
            is LoadState.Error -> ErrorView(loadState.ex, loadState.reason, loadState.allowRetry, onRetry = { viewModel.load(context) }, onLogout = { userManager.logout() })
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

            CreateTemplateView(viewModel)

            ExportImageViews(viewModel)
        }
    }

    @Composable
    fun ExportImageViews(viewModel: ScheduleTemplateListViewModel) {
        Column {
            ExportButton(viewModel)
            ImportButton(viewModel)
        }
    }
}

@Composable
fun ExportButton(viewModel: ScheduleTemplateListViewModel) {
    var selectedFile by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            selectedFile = uri
        }
    )

    OutlinedESButton(
        onClick = {
            launcher.launch("schedule_templates.json")
        }
    ) {
        Row {
            Icon(
                imageVector = Icons.Default.Upload,
                contentDescription = "Export",
                modifier = Modifier.padding(end = 8.dp)
            )

            Text(stringResource(R.string.export_templates))
        }

        selectedFile?.let { uri ->
            viewModel.exportTo(uri, context)
        }
    }
}

@Composable
fun ImportButton(viewModel: ScheduleTemplateListViewModel) {
    val context = LocalContext.current
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showConfirm by remember { mutableStateOf(false) }
    var replaceExistingTemplates by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                pendingImportUri = uri
            }
        }
    )

    OutlinedESButton(
        onClick = { showConfirm = true }
    ) {
        Row {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Import",
                modifier = Modifier.padding(end = 8.dp)
            )

            Text(stringResource(R.string.import_templates))
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = {
                showConfirm = false
                pendingImportUri = null
            },
            title = { Text(stringResource(R.string.import_templates)) },
            text = { Text(stringResource(R.string.do_you_want_to_replace_your_existing_templates_with_the_imported_templates)) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    replaceExistingTemplates = true
                    launcher.launch(arrayOf("application/json"))
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirm = false
                    replaceExistingTemplates = false
                    launcher.launch(arrayOf("application/json"))
                }) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    pendingImportUri?.let {
        pendingImportUri?.let { viewModel.importFrom(it, context, replaceExistingTemplates) }
        pendingImportUri = null
    }
}

@Composable
fun CreateTemplateView(viewModel: ScheduleTemplateListViewModel) {
    val context = LocalContext.current
    val presentCreateAlert = remember { mutableStateOf(false) }

    ESButton(
        onClick = { presentCreateAlert.value = true }
    ) {
        Text(
            stringResource(id = R.string.create_new_template),
            color = colorScheme.onPrimary,
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
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
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
