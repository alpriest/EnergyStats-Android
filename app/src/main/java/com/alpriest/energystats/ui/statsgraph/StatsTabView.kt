package com.alpriest.energystats.ui.statsgraph

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
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
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoFoxESSNetworking
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.paramsgraph.showExportMethodSelection
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate

sealed class StatsDisplayMode {
    data class Day(val date: LocalDate) : StatsDisplayMode()
    data class Month(val month: Int, val year: Int) : StatsDisplayMode()
    data class Year(val year: Int) : StatsDisplayMode()

    fun unit(): String {
        return when (this) {
            is Day -> "Hour"
            is Month -> "Day"
            is Year -> "Month"
        }
    }
}

@Composable
fun StatsTabView(
    viewModel: StatsTabViewModel,
    filePathChooser: (filename: String, action: (Uri) -> Unit) -> Unit?,
    themeStream: MutableStateFlow<AppTheme>
) {
    val scrollState = rememberScrollState()
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val graphShowing = viewModel.showingGraphStream.collectAsState().value
    val showingApproximations = remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.displayModeStream) {
        isLoading = true
        viewModel.displayModeStream
            .onEach { viewModel.load() }
            .collect { isLoading = false }
    }

    if (isLoading) {
        Text(stringResource(R.string.loading))
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(scrollState)
        ) {
            StatsDatePickerView(viewModel = StatsDatePickerViewModel(viewModel.displayModeStream), viewModel.showingGraphStream, modifier = Modifier.padding(bottom = 24.dp))

            if (graphShowing) {
                StatsGraphView(viewModel = viewModel, themeStream, modifier = Modifier.padding(bottom = 24.dp))
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
                Row(modifier = Modifier.clickable {
                    showExportMethodSelection(context, viewModel.exportFileName, filePathChooser, viewModel)
                }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    Text(stringResource(R.string.export_csv_data))
                }
            }
        }
    }
}

@Preview(widthDp = 400, heightDp = 800)
@Composable
fun StatsGraphTabViewPreview() {
    StatsTabView(
        StatsTabViewModel(FakeConfigManager(), DemoFoxESSNetworking()) { _, _ -> null },
        { _, _ -> },
        MutableStateFlow(AppTheme.preview())
    )
}