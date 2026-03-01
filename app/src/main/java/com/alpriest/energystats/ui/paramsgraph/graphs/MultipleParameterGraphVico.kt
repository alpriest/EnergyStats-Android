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
fun MultipleParameterGraphVico(
    viewModel: ParametersGraphTabViewModel,
    appSettingsStream: StateFlow<AppSettings>,
    producerData: List<ParametersGraphProducerData>
) {
    val valuesAtTimeState = viewModel.valuesAtTimeStream.collectAsState().value

    producerData.forEach { producerData ->
        val valuesForThisUnit: List<DateTimeFloatEntry> = remember(producerData.unit, valuesAtTimeState) {
            valuesAtTimeState.filter { it.key.unit == producerData.unit }.values.flatten()
        }

        LoadStateParameterGraphVico(
            data = producerData.entries,
            yAxisScale = producerData.yScale(),
            viewModel = viewModel,
            appSettingsStream = appSettingsStream,
            showYAxisUnit = true,
            valuesAtTimeStream = valuesForThisUnit
        )

        ParameterGraphVariableTogglesView(
            viewModel = viewModel,
            unit = producerData.unit,
            modifier = Modifier.padding(bottom = 44.dp, top = 6.dp),
            appSettingsStream = appSettingsStream
        )
    }
}