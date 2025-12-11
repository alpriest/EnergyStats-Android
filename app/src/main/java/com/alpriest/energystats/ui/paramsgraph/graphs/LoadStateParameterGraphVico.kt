package com.alpriest.energystats.ui.paramsgraph.graphs

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.ui.dialog.LoadingOverlayView
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.paramsgraph.DateTimeFloatEntry
import com.alpriest.energystats.ui.paramsgraph.ParameterGraphViewVico
import com.alpriest.energystats.ui.paramsgraph.ParametersGraphTabViewModel
import com.alpriest.energystats.ui.theme.AppTheme
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import kotlinx.coroutines.flow.MutableStateFlow

val VariableKey = ExtraStore.Key<Variable>()
val VariablesKey = ExtraStore.Key<List<Variable>>()

@Composable
fun LoadStateParameterGraphVico(
    data: List<List<DateTimeFloatEntry>>,
    chartColors: List<Color>,
    yAxisScale: AxisScale,
    viewModel: ParametersGraphTabViewModel,
    themeStream: MutableStateFlow<AppTheme>,
    showYAxisUnit: Boolean,
    userManager: UserManaging,
    valuesAtTimeStream: List<DateTimeFloatEntry>
) {
    val loadState = viewModel.uiState.collectAsState().value.state
    val modelProducer = remember {
        CartesianChartModelProducer()
    }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            if ( data.firstOrNull()?.isNotEmpty() == true) {
                extras { extraStore ->
                    extraStore[VariablesKey] = data.map {
                        it.first().type
                    }
                    extraStore[VariableKey] = data.first().first().type
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

    Box(contentAlignment = Alignment.Companion.Center) {
        ParameterGraphViewVico(
            modelProducer,
            chartColors,
            yAxisScale,
            viewModel = viewModel,
            themeStream = themeStream,
            showYAxisUnit = showYAxisUnit,
            userManager = userManager,
            valuesAtTimeStream
        )

        when (loadState) {
            is LoadState.Error -> Text(stringResource(R.string.error))
            is LoadState.Active -> LoadingOverlayView()
            is LoadState.Inactive -> {}
        }
    }
}