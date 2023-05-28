package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.statsgraph.StatsGraphTabViewModel
import com.alpriest.energystats.ui.statsgraph.StatsGraphVariableTogglesView
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

@Composable
fun ParametersGraphTabView(viewModel: ParametersGraphTabViewModel, themeStream: MutableStateFlow<AppTheme>) {
    val scrollState = rememberScrollState()
    var isLoading by remember { mutableStateOf(false) }

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
            ParameterGraphHeaderView(viewModel = viewModel, modifier = Modifier.padding(bottom = 24.dp))

            ParameterGraphView(viewModel = viewModel, modifier = Modifier.padding(bottom = 24.dp))

            ParameterGraphVariableTogglesView(viewModel = viewModel, modifier = Modifier.padding(bottom = 44.dp, top = 6.dp), themeStream = themeStream)

            Text(
                text = "Parameters are updated every 5 minutes by FoxESS and only available for a single day at a time",
                fontSize = 12.sp,
                color = DimmedTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 44.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewParameterGraphHeaderView() {
    ParameterGraphHeaderView(viewModel = ParametersGraphTabViewModel(configManager = FakeConfigManager(), networking = DemoNetworking()))
}
