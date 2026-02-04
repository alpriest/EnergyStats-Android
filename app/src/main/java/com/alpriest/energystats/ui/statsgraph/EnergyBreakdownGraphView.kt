package com.alpriest.energystats.ui.statsgraph

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alpriest.energystats.models.colour
import com.alpriest.energystats.shared.helpers.kW
import com.alpriest.energystats.shared.models.ReportVariable
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.stacked
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.component.LineComponent

enum class EnergyBreakdownType {
    Inputs,
    Outputs;

    fun title(value: Double): String {
        return when (this) {
            Inputs -> "Energy Sources ${value.kW(1)}"
            Outputs -> "Energy Uses ${value.kW(1)}"
        }
    }

    val graphX: List<Double>
        get() {
            return when (this) {
                Inputs -> listOf(0.0)
                Outputs -> listOf(1.0)
            }
        }

    val types: List<ReportVariable>
        get() {
            return when (this) {
                Inputs -> listOf(ReportVariable.DischargeEnergyToTal, ReportVariable.GridConsumption, ReportVariable.PvEnergyToTal)
                Outputs -> listOf(ReportVariable.ChargeEnergyToTal, ReportVariable.FeedIn, ReportVariable.Loads)
            }
        }
}

@Composable
fun EnergyBreakdownGraphView(viewModel: StatsTabViewModel) {
    val appSettingsStream = viewModel.appSettingsStream
    val totalsStream = viewModel.totalsStream.collectAsStateWithLifecycle().value
    val modelProducer = remember { CartesianChartModelProducer() }
    val scrollState = rememberVicoScrollState(scrollEnabled = false)
    val zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content)
    val chartColors = (EnergyBreakdownType.Inputs.types + EnergyBreakdownType.Outputs.types).map { it.colour(appSettingsStream) }
    val totals = mutableMapOf<EnergyBreakdownType, Double>()

    LaunchedEffect(totalsStream) {
        modelProducer.runTransaction {
            if (totalsStream.isNotEmpty()) {
                columnSeries {
                    EnergyBreakdownType.Inputs.types.forEach {
                        series(x = EnergyBreakdownType.Inputs.graphX, listOfNotNull(totalsStream[it]))
                    }
                    EnergyBreakdownType.Outputs.types.forEach {
                        series(x = EnergyBreakdownType.Outputs.graphX, listOfNotNull(totalsStream[it]))
                    }
                }

                totals[EnergyBreakdownType.Inputs] = totalsStream.filter { EnergyBreakdownType.Inputs.types.contains(it.key) }.values.sum()
                totals[EnergyBreakdownType.Outputs] = totalsStream.filter { EnergyBreakdownType.Outputs.types.contains(it.key) }.values.sum()
            }
        }
    }

    if (totalsStream.isNotEmpty()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnsLayer(chartColors),
                endAxis = VerticalAxis.rememberEnd(
                    guideline = rememberAxisGuidelineComponent()
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    guideline = null,
                    valueFormatter = EnergyBreakdownBottomAxisValueFormatter(totals)
                ),
            ),
            modelProducer = modelProducer,
            scrollState = scrollState,
            animateIn = false,
            animationSpec = null,
            zoomState = zoomState
        )
    }
}

private class EnergyBreakdownBottomAxisValueFormatter(private val totals: MutableMap<EnergyBreakdownType, Double>) : CartesianValueFormatter {
    override fun format(
        context: CartesianMeasuringContext,
        value: Double,
        verticalAxisPosition: Axis.Position.Vertical?,
    ): CharSequence {
        val type = when (value) {
            0.0 -> EnergyBreakdownType.Inputs
            else -> EnergyBreakdownType.Outputs
        }

        return totals[type]?.let {
            return type.title(it)
        } ?: "loading"
    }
}

@Composable
private fun rememberColumnsLayer(chartColors: List<Color>): ColumnCartesianLayer {
    val lineColumnProvider = remember(chartColors) {
        ColumnCartesianLayer.ColumnProvider.series(
            *chartColors.map { color ->
                LineComponent(
                    fill(color),
                    thicknessDp = 25.0f,
                    strokeFill = fill(color)
                )
            }.toTypedArray(),
        )
    }

    return rememberColumnCartesianLayer(
        lineColumnProvider,
        verticalAxisPosition = Axis.Position.Vertical.End,
        mergeMode = { ColumnCartesianLayer.MergeMode.stacked() }
    )
}
