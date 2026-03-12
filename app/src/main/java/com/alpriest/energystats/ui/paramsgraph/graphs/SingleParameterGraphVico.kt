package com.alpriest.energystats.ui.paramsgraph.graphs

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.ui.paramsgraph.DateTimeFloatEntry
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphVariableTogglesView
import com.alpriest.energystats.ui.paramsgraph.ParametersGraphProducerData
import com.alpriest.energystats.ui.paramsgraph.ParametersGraphTabViewModel
import com.alpriest.energystats.ui.paramsgraph.yScale
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SingleParameterGraphVico(
    viewModel: ParametersGraphTabViewModel,
    appSettingsStream: StateFlow<AppSettings>,
    producerData: List<ParametersGraphProducerData>
) {
    val valuesAtTimeState = viewModel.valuesAtTimeStream.collectAsState().value
    val valuesForThisUnit: List<DateTimeFloatEntry> = remember(valuesAtTimeState) {
        valuesAtTimeState.values.flatten()
    }
    val enabledGraphVariables = viewModel.graphVariablesStream.collectAsState().value
        .filter { it.enabled }
        .map { it.type }

    val allData = remember(producerData) {
        producerData
            .filter {
                val entryTypes = it.entries.mapNotNull { entryType -> entryType.firstOrNull()?.type }
                entryTypes.any { entryType -> enabledGraphVariables.contains(entryType) }
            }
            .flatMap { it.entries }
    }

    val yAxisScale = remember(producerData, enabledGraphVariables) {
        val allAxisScales = producerData
            .filter {
                val entryTypes = it.entries.mapNotNull { entryType -> entryType.firstOrNull()?.type }
                entryTypes.any { entryType -> enabledGraphVariables.contains(entryType) }
            }
            .map { it.yScale() }

        AxisScale(
            min = allAxisScales.minOfOrNull { it.min ?: 10000.0f } ?: 10000.0f,
            max = allAxisScales.maxOfOrNull { it.max ?: -10000.0f } ?: -10000.0f
        )
    }

    LoadStateParameterGraphVico(
        data = allData,
        yAxisScale = yAxisScale,
        viewModel = viewModel,
        appSettingsStream = appSettingsStream,
        showYAxisUnit = false,
        valuesForThisUnit
    )

    ParameterGraphVariableTogglesView(viewModel = viewModel, null, modifier = Modifier.padding(bottom = 44.dp, top = 6.dp), appSettingsStream = appSettingsStream)
}