package com.alpriest.energystats.ui.paramsgraph

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.tabs.TopBarSettings
import com.alpriest.energystats.ui.dialog.LoadingOverlayView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.alpriest.energystats.ui.summary.DemoSolarForecasting
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ParametersGraphTabViewModelFactory(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val onWriteTempFile: (String, String) -> Uri?,
    private val graphVariablesStream: MutableStateFlow<List<ParameterGraphVariable>>,
    private val solarForecastProvider: () -> SolcastCaching
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ParametersGraphTabViewModel(network, configManager, onWriteTempFile, graphVariablesStream, solarForecastProvider) as T
    }
}

class ParametersGraphTabView(
    private val topBarSettings: MutableState<TopBarSettings>,
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val userManager: UserManaging,
    private val onWriteTempFile: (String, String) -> Uri?,
    private val graphVariablesStream: MutableStateFlow<List<ParameterGraphVariable>>,
    private val navController: NavHostController,
    private val filePathChooser: (filename: String, action: (Uri) -> Unit) -> Unit?,
    private val solarForecastProvider: () -> SolcastCaching
) {
    @Composable
    fun Content(
        viewModel: ParametersGraphTabViewModel = viewModel(
            factory = ParametersGraphTabViewModelFactory(
                network,
                configManager,
                onWriteTempFile,
                graphVariablesStream,
                solarForecastProvider
            )
        ),
        themeStream: MutableStateFlow<AppTheme>
    ) {
        val scrollState = rememberScrollState()
        val hasData = viewModel.hasDataStream.collectAsState().value
        val selectedValues = viewModel.valuesAtTimeStream.collectAsState().value
        val selectedDateTime = selectedValues.firstOrNull()?.localDateTime
        val context = LocalContext.current
        val loadState = viewModel.uiState.collectAsState().value.state
        topBarSettings.value = TopBarSettings(true, null, {
            ParameterGraphHeaderView(viewModel = viewModel, navController = navController, configManager = configManager)
        }, null)

        MonitorAlertDialog(viewModel, userManager)

        LaunchedEffect(viewModel.displayModeStream) {
            viewModel.load()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(12.dp)
        ) {
            when (loadState) {
                is LoadState.Error ->
                    Text(stringResource(R.string.error))

                is LoadState.Active ->
                    Box(
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingOverlayView()
                    }

                is LoadState.Inactive -> {
                    if (hasData) {
                        LoadedData(selectedDateTime, viewModel, themeStream)
                    } else {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "No data. Try changing your filters",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Text(
                text = stringResource(id = R.string.parameters_update_description),
                fontSize = 12.sp,
                color = DimmedTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 44.dp, bottom = 22.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.clickable {
                        showExportMethodSelection(context, viewModel.exportFileName, filePathChooser, viewModel)
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    Text(stringResource(R.string.export_csv_data))
                }
            }
        }
    }

    @Composable
    private fun LoadedData(selectedDateTime: LocalDateTime?, viewModel: ParametersGraphTabViewModel, themeStream: MutableStateFlow<AppTheme>) {
        val producerAxisScalePairs = viewModel.producers.collectAsState()
        val allChartColors = viewModel.chartColorsStream.collectAsState().value

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            viewModel.lastMarkerModelStream.value?.let {
                selectedDateTime?.let {
                    Row(modifier = Modifier.clickable {
                        viewModel.lastMarkerModelStream.value = null
                        viewModel.valuesAtTimeStream.value = listOf()
                    }) {
                        Text(
                            text = it.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(end = 6.dp)
                        )

                        Text(
                            text = stringResource(R.string.clear),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } ?: run {
                Text(
                    stringResource(R.string.touch_the_graph_to_see_values_at_that_time),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (configManager.separateParameterGraphsByUnit) {
            MultipleParameterGraphVico1(allChartColors, viewModel, themeStream, userManager, producerAxisScalePairs)
        } else {
            SingleParameterGraphVico1(
                allChartColors,
                viewModel,
                themeStream,
                userManager,
                producerAxisScalePairs
            )

            ParameterGraphVariableTogglesView(viewModel = viewModel, null, modifier = Modifier.padding(bottom = 44.dp, top = 6.dp), themeStream = themeStream)
        }
    }
}

fun showExportMethodSelection(context: Context, filename: String, filePathChooser: (filename: String, action: (Uri) -> Unit) -> Unit?, viewModel: ExportProviding) {
    val items = arrayOf(context.getString(R.string.export_save_to_a_local_file), context.getString(R.string.export_share_or_open_with_another_app))

    AlertDialog.Builder(context)
        .setTitle(context.getString(R.string.choose_export_method))
        .setItems(items) { _, which ->
            when (which) {
                0 -> {
                    filePathChooser(filename) {
                        viewModel.exportTo(context, it)
                    }
                }

                1 -> {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, viewModel.exportFileUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        type = "text/csv"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }
            }
        }
        .setNegativeButton(context.getString(R.string.cancel), null)
        .show()
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun PreviewParameterGraphHeaderView() {
    ParameterGraphHeaderView(
        viewModel = ParametersGraphTabViewModel(
            configManager = FakeConfigManager(),
            networking = DemoNetworking(),
            onWriteTempFile = { _, _ -> null },
            graphVariablesStream = MutableStateFlow(listOf()),
            solarForecastProvider = { DemoSolarForecasting() }
        ),
        navController = NavHostController(LocalContext.current),
        configManager = FakeConfigManager()
    )
}
