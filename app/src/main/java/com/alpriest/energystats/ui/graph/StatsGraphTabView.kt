package com.alpriest.energystats.ui.graph

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.theme.DimmedTextColor
import java.time.LocalDate
import java.util.*

sealed class StatsDisplayMode {
    data class Day(val date: LocalDate) : StatsDisplayMode()
    data class Month(val month: Int, val year: Int) : StatsDisplayMode()
    data class Year(val year: Int) : StatsDisplayMode()

    fun unit(): Int {
        return when (this) {
            is Day -> Calendar.HOUR
            is Month -> Calendar.DAY_OF_MONTH
            is Year -> Calendar.MONTH
        }
    }
}

@Composable
fun StatsGraphTabView(viewModel: StatsGraphTabViewModel) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(12.dp)
        .verticalScroll(scrollState)
    ) {
        StatsDatePickerView(viewModel = StatsDatePickerViewModel(viewModel.displayModeStream))

        StatsGraphView(viewModel = viewModel, modifier = Modifier.padding(bottom = 12.dp))

        StatsGraphVariableTogglesView(viewModel = viewModel, modifier = Modifier.padding(bottom = 44.dp, top = 6.dp))

        Text(
            text = "Stats are aggregated by FoxESS into 1 hr, 1 day or 1 month totals",
            fontSize = 12.sp,
            color = DimmedTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 44.dp)
        )
    }
}

@Preview(widthDp = 400, heightDp = 800)
@Composable
fun StatsGraphTabViewPreview() {
    StatsGraphTabView(StatsGraphTabViewModel(FakeConfigManager(), DemoNetworking()))
}
