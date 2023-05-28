package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

@Composable
fun ParametersGraphTabView(viewModel: ParametersGraphTabViewModel, themeStream: MutableStateFlow<AppTheme>) {
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
        Column {
            ParameterGraphHeaderView(viewModel = viewModel,)

            ParameterGraphView(viewModel = viewModel, modifier = Modifier.padding(bottom = 24.dp))
        }
    }
}

@Preview
@Composable
fun PreviewParameterGraphHeaderView() {
    ParameterGraphHeaderView(viewModel = ParametersGraphTabViewModel(configManager = FakeConfigManager(), networking = DemoNetworking()),)
}