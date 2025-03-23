package com.alpriest.energystats.ui.statsgraph

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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.LoadingOverlayView
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.paramsgraph.showExportMethodSelection
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

sealed class StatsDisplayMode {
    data class Day(val date: LocalDate) : StatsDisplayMode()
    data class Month(val month: Int, val year: Int) : StatsDisplayMode()
    data class Year(val year: Int) : StatsDisplayMode()
    data class Custom(val start: LocalDate, val end: LocalDate) : StatsDisplayMode()

    fun unit(): String {
        return when (this) {
            is Day -> "Hour"
            is Month -> "Day"
            is Year -> "Month"
            is Custom -> "Day"
        }
    }
}

class StatsTabViewModelFactory(
    private val configManager: ConfigManaging,
    private val networking: Networking,
    private val onWriteTempFile: (String, String) -> Uri?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StatsTabViewModel(configManager, networking, onWriteTempFile) as T
    }
}

class StatsTabView(
    private val configManager: ConfigManaging,
    private val network: Networking,
    private val onWriteTempFile: (String, String) -> Uri?,
    private val filePathChooser: (filename: String, action: (Uri) -> Unit) -> Unit?,
    private val themeStream: MutableStateFlow<AppTheme>,
    private val userManager: UserManaging
) {
    @Composable
    fun Content(
        viewModel: StatsTabViewModel = viewModel(factory = StatsTabViewModelFactory(configManager, network, onWriteTempFile)),
    ) {
        val scrollState = rememberScrollState()
        val context = LocalContext.current
        val graphShowing = viewModel.showingGraphStream.collectAsState().value
        val showingApproximations = remember { mutableStateOf(false) }
        val loadState = viewModel.uiState.collectAsState().value.state
        val showTipDialog = remember { mutableStateOf(false) }
        val tipKitManager = remember { TipKitManager() }

        MonitorAlertDialog(viewModel, userManager)

        LaunchedEffect(viewModel.displayModeStream) {
            viewModel.displayModeStream.collectLatest { viewModel.load(context) }
        }
        trackScreenView("Stats Tab", "StatsTabView")

        LaunchedEffect(Unit) {
            tipKitManager.checkAndShow(TipType.statsGraphDecimalPlacesFixedTo1, context)
        }

        tipKitManager.activeTip.value?.let { tip ->
            TipDialog(tip, context) {
                tipKitManager.dismiss()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(12.dp)
        ) {
            StatsDatePickerView(viewModel = StatsDatePickerViewModel(viewModel.displayModeStream), viewModel.showingGraphStream, modifier = Modifier.padding(bottom = 24.dp))

            Box(contentAlignment = Alignment.Center) {
                if (graphShowing) {
                    StatsGraphView(viewModel = viewModel, themeStream, modifier = Modifier.padding(bottom = 24.dp))
                }

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

                    is LoadState.Inactive -> {}
                }
            }

            StatsGraphVariableTogglesView(viewModel = viewModel, modifier = Modifier.padding(bottom = 44.dp, top = 6.dp), themeStream = themeStream)

            viewModel.approximationsViewModelStream.collectAsState().value?.let {
                ApproximationView(viewModel = it, modifier = Modifier, themeStream = themeStream, showingApproximations = showingApproximations)
            }

            Text(
                text = stringResource(R.string.stats_are_aggregated_by_foxess_into_1_hr_1_day_or_1_month_totals),
                fontSize = 12.sp,
                color = DimmedTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 44.dp, bottom = 22.dp)
                    .fillMaxWidth()
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
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = colorScheme.onSecondary
                    )
                    Text(
                        stringResource(R.string.export_csv_data),
                        color = colorScheme.onSecondary
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 400, heightDp = 800)
@Composable
fun StatsGraphTabViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        StatsTabView(
            FakeConfigManager(),
            DemoNetworking(),
            { _, _ -> null },
            { _, _ -> },
            MutableStateFlow(AppTheme.demo()),
            FakeUserManager()
        ).Content()
    }
}
