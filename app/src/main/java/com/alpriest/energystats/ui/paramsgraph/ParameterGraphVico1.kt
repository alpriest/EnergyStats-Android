package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.dialog.LoadingOverlayView
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.theme.AppTheme
import com.patrykandpatrick.vico1.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun MultipleParameterGraphVico1(
    allChartColors: Map<String, List<Color>>,
    viewModel: ParametersGraphTabViewModel,
    themeStream: MutableStateFlow<AppTheme>,
    userManager: UserManaging,
    producerAxisScalePairs: State<Map<String, Pair<List<List<DateTimeFloatEntry>>, AxisScale>>>
) {
    producerAxisScalePairs.value.forEach { (unit, producerAxisScale) ->
        allChartColors[unit]?.let {
            val vico1Models = remember(producerAxisScale.first) {
                producerAxisScale.first.map { series: List<DateTimeFloatEntry> ->
                    series.map { it.toVico1() }
                }
            }

            val chartProducer = remember(vico1Models) {
                ChartEntryModelProducer(*vico1Models.toTypedArray())
            }

            ParameterGraphVico1(
                chartProducer,
                producerAxisScale.second,
                chartColors = it,
                viewModel,
                themeStream,
                showYAxisUnit = true,
                userManager
            )

            ParameterGraphVariableTogglesView(viewModel = viewModel, unit, modifier = Modifier.padding(bottom = 44.dp, top = 6.dp), themeStream = themeStream)
        }
    }
}

@Composable
fun SingleParameterGraphVico1(
    allChartColors: Map<String, List<Color>>,
    viewModel: ParametersGraphTabViewModel,
    themeStream: MutableStateFlow<AppTheme>,
    userManager: UserManaging,
    producerAxisScalePairs: State<Map<String, Pair<List<List<DateTimeFloatEntry>>, AxisScale>>>
) {
    val chartColors = remember(allChartColors) { allChartColors.values.flatten() }
    val allEntries = remember(producerAxisScalePairs.value) {
        producerAxisScalePairs.value.values.flatMap {
            it.first.map { series: List<DateTimeFloatEntry> ->
                series.map { it.toVico1() }
            }
        }
    }
    val yAxisScale = remember(producerAxisScalePairs.value) {
        val allAxisScales = producerAxisScalePairs.value.map { it.value.second }
        AxisScale(min = allAxisScales.minOf { it.min ?: 10000.0f }, max = allAxisScales.maxOf { it.max ?: -10000.0f })
    }
    val producer: ChartEntryModelProducer = remember(allEntries) { ChartEntryModelProducer(allEntries) }

    ParameterGraphVico1(
        producer,
        yAxisScale,
        chartColors,
        viewModel,
        themeStream,
        showYAxisUnit = false,
        userManager
    )
}

@Composable
private fun ParameterGraphVico1(
    producer: ChartEntryModelProducer,
    yAxisScale: AxisScale,
    chartColors: List<Color>,
    viewModel: ParametersGraphTabViewModel,
    themeStream: MutableStateFlow<AppTheme>,
    showYAxisUnit: Boolean,
    userManager: UserManaging
) {
    val loadState = viewModel.uiState.collectAsState().value.state

    Box(contentAlignment = Alignment.Companion.Center) {
        ParameterGraphViewVico1(
            producer,
            yAxisScale,
            chartColors = chartColors,
            viewModel = viewModel,
            themeStream,
            showYAxisUnit = showYAxisUnit,
            userManager
        )

        when (loadState) {
            is LoadState.Error ->
                Text(stringResource(R.string.error))

            is LoadState.Active ->
                LoadingOverlayView()

            is LoadState.Inactive -> {}
        }
    }
}