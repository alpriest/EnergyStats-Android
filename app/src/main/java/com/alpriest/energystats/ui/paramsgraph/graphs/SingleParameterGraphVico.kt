package com.alpriest.energystats.ui.paramsgraph.graphs

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.paramsgraph.DateTimeFloatEntry
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphVariableTogglesView
import com.alpriest.energystats.ui.paramsgraph.ParametersGraphTabViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SingleParameterGraphVico(
    viewModel: ParametersGraphTabViewModel,
    appSettingsStream: StateFlow<AppSettings>,
    userManager: UserManaging,
    producerAxisScalePairs: Map<String, Pair<List<List<DateTimeFloatEntry>>, AxisScale>>
) {
    val chartColors = colorsForVariables(viewModel.viewDataState.collectAsState().value.graphVariables, appSettingsStream).flatMap { it.value }
    val valuesAtTimeState = viewModel.valuesAtTimeStream.collectAsState().value
    val valuesForThisUnit: List<DateTimeFloatEntry> = remember(valuesAtTimeState) {
        valuesAtTimeState.values.flatten()
    }

    val allData = remember(producerAxisScalePairs) {
        producerAxisScalePairs.values.flatMap { it.first }
    }
    val yAxisScale = remember(producerAxisScalePairs) {
        val allAxisScales = producerAxisScalePairs.map { it.value.second }
        AxisScale(
            min = allAxisScales.minOf { it.min ?: 10000.0f },
            max = allAxisScales.maxOf { it.max ?: -10000.0f }
        )
    }

    LoadStateParameterGraphVico(
        data = allData,
        chartColors,
        yAxisScale = yAxisScale,
        viewModel = viewModel,
        appSettingsStream = appSettingsStream,
        showYAxisUnit = false,
        userManager = userManager,
        valuesForThisUnit
    )

    ParameterGraphVariableTogglesView(viewModel = viewModel, null, modifier = Modifier.Companion.padding(bottom = 44.dp, top = 6.dp), appSettingsStream = appSettingsStream)
}