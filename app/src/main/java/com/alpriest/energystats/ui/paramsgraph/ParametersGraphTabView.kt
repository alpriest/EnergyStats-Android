package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ParametersGraphTabView(viewModel: ParametersGraphTabViewModel, themeStream: MutableStateFlow<AppTheme>) {
    val scrollState = rememberScrollState()
    var isLoading by remember { mutableStateOf(false) }
    val hasData = viewModel.hasDataStream.collectAsState().value

    LaunchedEffect(viewModel.displayModeStream) {
        isLoading = true
        viewModel.load()
        isLoading = false
    }

    if (isLoading) {
        Column(modifier = Modifier
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
            ParameterGraphHeaderView(viewModel = viewModel, modifier = Modifier.padding(bottom = 24.dp))

            if (hasData) {
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
