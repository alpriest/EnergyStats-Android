package com.alpriest.energystats.ui.graph

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking

@Composable
fun StatsGraphVariableTogglesView(viewModel: StatsGraphTabViewModel, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text("todo")
    }
}

@Composable
@Preview
fun StatsGraphVariableTogglesViewPreview() {
    StatsGraphVariableTogglesView(StatsGraphTabViewModel(FakeConfigManager(), DemoNetworking()))
}