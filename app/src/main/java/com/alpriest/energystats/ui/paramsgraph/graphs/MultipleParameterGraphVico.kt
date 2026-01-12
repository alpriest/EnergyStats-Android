package com.alpriest.energystats.ui.paramsgraph.graphs

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.paramsgraph.DateTimeFloatEntry
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphVariableTogglesView
import com.alpriest.energystats.ui.paramsgraph.ParametersGraphTabViewModel
import com.alpriest.energystats.shared.models.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun MultipleParameterGraphVico(
    viewModel: ParametersGraphTabViewModel,
    themeStream: MutableStateFlow<AppSettings>,
    userManager: UserManaging,
    producerAxisScalePairs: Map<String, Pair<List<List<DateTimeFloatEntry>>, AxisScale>>
) {
    val allChartColors = viewModel.viewDataState.collectAsState().value.colors
    val valuesAtTimeState = viewModel.valuesAtTimeStream.collectAsState().value

    producerAxisScalePairs.forEach { (unit, producerAxisScale) ->
        allChartColors[unit]?.let { colors ->
            val valuesForThisUnit: List<DateTimeFloatEntry> = remember(unit, valuesAtTimeState) {
                valuesAtTimeState.filter { it.key.unit == unit }.values.flatten()
            }

            LoadStateParameterGraphVico(
                data = producerAxisScale.first,
                colors,
                yAxisScale = producerAxisScale.second,
                viewModel = viewModel,
                themeStream = themeStream,
                showYAxisUnit = true,
                userManager = userManager,
                valuesAtTimeStream = valuesForThisUnit
            )

            ParameterGraphVariableTogglesView(
                viewModel = viewModel,
                unit = unit,
                modifier = Modifier.Companion.padding(bottom = 44.dp, top = 6.dp),
                themeStream = themeStream
            )
        }
    }
}