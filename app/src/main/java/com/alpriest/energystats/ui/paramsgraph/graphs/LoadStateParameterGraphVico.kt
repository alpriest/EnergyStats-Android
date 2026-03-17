package com.alpriest.energystats.ui.paramsgraph.graphs

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.shared.models.Variable
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.paramsgraph.DateTimeFloatEntry
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphViewVico
import com.alpriest.energystats.ui.paramsgraph.ParametersGraphTabViewModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import kotlinx.coroutines.flow.StateFlow

val VariableKey = ExtraStore.Key<Variable>()

@Composable
fun LoadStateParameterGraphVico(
    data: List<List<DateTimeFloatEntry>>,
    yAxisScale: AxisScale,
    viewModel: ParametersGraphTabViewModel,
    appSettingsStream: StateFlow<AppSettings>,
    showYAxisUnit: Boolean,
    valuesAtTimeStream: List<DateTimeFloatEntry>
) {
    val loadState = viewModel.uiState.collectAsState().value.state
    val modelProducer = remember { CartesianChartModelProducer() }
    val chartColors = data.map {
        it.first().type.colour(appSettingsStream)
    }

    LaunchedEffect(data) {
        if (data.isNotEmpty()) {
            modelProducer.runTransaction {
                if (data.firstOrNull()?.isNotEmpty() == true) {
                    extras { extraStore ->
                        data.firstOrNull()?.firstOrNull()?.type?.let {
                            extraStore[VariableKey] = it
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
        }
    }

    Box(contentAlignment = Alignment.Center) {
        ParameterGraphViewVico(
            modelProducer,
            chartColors,
            yAxisScale,
            viewModel = viewModel,
            appSettingsStream = appSettingsStream,
            showYAxisUnit = showYAxisUnit,
            valuesAtTimeStream
        )

        when (loadState) {
            is LoadState.Error -> Text(stringResource(R.string.error))
            is LoadState.Active -> LoadingView(LoadState.Active.Loading)
            is LoadState.Inactive -> {}
        }
    }
}