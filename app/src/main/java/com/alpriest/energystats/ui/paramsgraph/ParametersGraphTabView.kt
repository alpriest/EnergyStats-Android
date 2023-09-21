package com.alpriest.energystats.ui.paramsgraph

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGraphVariableChooserView
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGraphVariableChooserViewModel
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGroup
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterVariableGroupEditorView
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterVariableGroupEditorViewModel
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

enum class ParametersScreen {
    Graph,
    ParameterChooser,
    ParameterGroupEditor
}

@Composable
fun NavigableParametersGraphTabView(
    configManager: ConfigManaging,
    network: Networking,
    onWriteTempFile: (String, String) -> Uri?,
    themeStream: MutableStateFlow<AppTheme>,
) {
    val navController = rememberNavController()
    val viewModel = ParametersGraphTabViewModel(configManager, network, onWriteTempFile)
    val graphVariables = viewModel.graphVariablesStream.collectAsState().value

    NavHost(
        navController = navController,
        startDestination = ParametersScreen.Graph.name
    ) {
        composable(ParametersScreen.Graph.name) {
            ParametersGraphTabView(viewModel, themeStream, navController)
        }

        composable(ParametersScreen.ParameterChooser.name) {
            ParameterGraphVariableChooserView(
                ParameterGraphVariableChooserViewModel(configManager, graphVariables, onApply = { viewModel.setGraphVariables(it) }),
                onCancel = { navController.popBackStack() },
                navController = navController
            )
        }

        composable(ParametersScreen.ParameterGroupEditor.name) {
            ParameterVariableGroupEditorView(
                ParameterVariableGroupEditorViewModel(configManager, graphVariables),
                navController = navController
            )
        }
    }
}

@Composable
fun ParametersGraphTabView(viewModel: ParametersGraphTabViewModel, themeStream: MutableStateFlow<AppTheme>, navController: NavHostController) {
    val scrollState = rememberScrollState()
    var isLoading by remember { mutableStateOf(false) }
    val hasData = viewModel.hasDataStream.collectAsState().value
    val selectedValues = viewModel.valuesAtTimeStream.collectAsState().value
    val selectedDateTime = selectedValues.firstOrNull()?.localDateTime

    LaunchedEffect(viewModel.displayModeStream) {
        isLoading = true
        viewModel.load()
        isLoading = false
    }

    val context = LocalContext.current

    if (isLoading) {
        Column(
            modifier = Modifier
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.loading))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(scrollState)
        ) {
            ParameterGraphHeaderView(viewModel = viewModel, modifier = Modifier.padding(bottom = 24.dp), navController)

            if (hasData) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    selectedDateTime?.let {
                        androidx.compose.material3.Text(
                            text = it.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)),
                            color = MaterialTheme.colors.onSecondary
                        )
                    } ?: run {
                        Text(stringResource(R.string.touch_the_graph_to_see_values_at_that_time))
                    }
                }

                ParameterGraphView(viewModel = viewModel, modifier = Modifier.padding(bottom = 24.dp))

                ParameterGraphVariableTogglesView(viewModel = viewModel, modifier = Modifier.padding(bottom = 44.dp, top = 6.dp), themeStream = themeStream)
            } else {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "No data. Try changing your filters",
                    textAlign = TextAlign.Center
                )
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
                Row(modifier = Modifier.clickable {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, viewModel.exportFileUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        type = "text/csv"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    Text(stringResource(R.string.export_csv_data))
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewParameterGraphHeaderView() {
    ParameterGraphHeaderView(
        viewModel = ParametersGraphTabViewModel(configManager = FakeConfigManager(), networking = DemoNetworking(), onWriteTempFile = { _, _ -> null }),
        navController = NavHostController(LocalContext.current)
    )
}
