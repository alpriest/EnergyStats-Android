package com.alpriest.energystats.ui.graph

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.models.kWh
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.DimmedTextColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun StatsGraphVariableTogglesView(viewModel: StatsGraphTabViewModel, themeStream: MutableStateFlow<AppTheme>, modifier: Modifier = Modifier) {
    val appTheme = themeStream.collectAsState().value
    val graphVariables = viewModel.graphVariablesStream.collectAsState()

    Column(modifier) {
        graphVariables.value.map {
            ToggleRowView(viewModel, it, appTheme)
        }
    }
}

@Composable
private fun ToggleRowView(
    viewModel: StatsGraphTabViewModel,
    it: StatsGraphVariable,
    appTheme: AppTheme
) {
    val textColor = if (it.enabled) Color.Black else DimmedTextColor

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .padding(bottom = 6.dp)
            .clickable {
                viewModel.toggleVisibility(it)
            }
    ) {
        Box(modifier = Modifier.padding(top = 4.dp)) {
            Canvas(modifier = Modifier.size(16.dp)) {
                drawCircle(
                    color = it.type.colour().copy(alpha = if (it.enabled) 1.0f else 0.5f),
                    radius = size.minDimension / 2,
                    center = Offset(size.width / 2, size.height / 2)
                )
            }
        }

        Column(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Row {
                Text(
                    it.type.title(),
                    color = textColor
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    viewModel.totalOf(it.type).kWh(appTheme.decimalPlaces),
                    color = textColor
                )
            }

            Text(
                it.type.description(),
                color = DimmedTextColor,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
@Preview
fun StatsGraphVariableTogglesViewPreview() {
    StatsGraphVariableTogglesView(StatsGraphTabViewModel(FakeConfigManager(), DemoNetworking()), themeStream = MutableStateFlow(AppTheme.preview()))
}