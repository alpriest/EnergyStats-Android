package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.ui.dialog.LoadingOverlayView
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.theme.AppTheme
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun MultipleParameterGraphVico2(
    viewModel: ParametersGraphTabViewModel,
    themeStream: MutableStateFlow<AppTheme>,
    userManager: UserManaging,
    producerAxisScalePairs: State<Map<String, Pair<List<List<DateTimeFloatEntry>>, AxisScale>>>
) {
    val allChartColors = viewModel.chartColorsStream.collectAsState().value

    producerAxisScalePairs.value.forEach { (unit, producerAxisScale) ->
        allChartColors[unit]?.let { colors ->
            LoadStateParameterGraphVico2(
                data = producerAxisScale.first,
                colors,
                yAxisScale = producerAxisScale.second,
                viewModel = viewModel,
                themeStream = themeStream,
                showYAxisUnit = true,
                userManager = userManager
            )

            ParameterGraphVariableTogglesView(
                viewModel = viewModel,
                unit = unit,
                modifier = Modifier.padding(bottom = 44.dp, top = 6.dp),
                themeStream = themeStream
            )
        }
    }
}

@Composable
fun SingleParameterGraphVico2(
    viewModel: ParametersGraphTabViewModel,
    themeStream: MutableStateFlow<AppTheme>,
    userManager: UserManaging,
    producerAxisScalePairs: State<Map<String, Pair<List<List<DateTimeFloatEntry>>, AxisScale>>>
) {
    val chartColors = viewModel.viewDataState.collectAsState().value.colors.values.flatten()

    val allData = remember(producerAxisScalePairs.value) {
        producerAxisScalePairs.value.values.flatMap { it.first }
    }
    val yAxisScale = remember(producerAxisScalePairs.value) {
        val allAxisScales = producerAxisScalePairs.value.map { it.value.second }
        AxisScale(
            min = allAxisScales.minOf { it.min ?: 10000.0f },
            max = allAxisScales.maxOf { it.max ?: -10000.0f }
        )
    }

    LoadStateParameterGraphVico2(
        data = allData,
        chartColors,
        yAxisScale = yAxisScale,
        viewModel = viewModel,
        themeStream = themeStream,
        showYAxisUnit = false,
        userManager = userManager
    )
}

val VariableKey = ExtraStore.Key<Variable>()
val VariablesKey = ExtraStore.Key<List<Variable>>()

@Composable
private fun LoadStateParameterGraphVico2(
    data: List<List<DateTimeFloatEntry>>,
    chartColors: List<Color>,
    yAxisScale: AxisScale,
    viewModel: ParametersGraphTabViewModel,
    themeStream: MutableStateFlow<AppTheme>,
    showYAxisUnit: Boolean,
    userManager: UserManaging
) {
    val loadState = viewModel.uiState.collectAsState().value.state
    val modelProducer = remember {
        CartesianChartModelProducer()
    }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            extras { extraStore ->
                extraStore[VariablesKey] = data.map {
                    it.first().type
                }
            }

            lineSeries {
                data.forEach { seriesEntries: List<DateTimeFloatEntry> ->
                    series(
                        x = seriesEntries.map { it.graphPoint },
                        y = seriesEntries.map { it.y.toDouble() }
                    )
                }
            }
        }
    }

    Box(contentAlignment = Alignment.Center) {
        ParameterGraphViewVico2(
            modelProducer,
            chartColors,
            yAxisScale,
            viewModel = viewModel,
            themeStream = themeStream,
            showYAxisUnit = showYAxisUnit,
            userManager = userManager
        )

        when (loadState) {
            is LoadState.Error -> Text(stringResource(R.string.error))
            is LoadState.Active -> LoadingOverlayView()
            is LoadState.Inactive -> {}
        }
    }
}
