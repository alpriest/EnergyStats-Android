package com.alpriest.energystats.ui.statsgraph

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alpriest.energystats.R
import com.alpriest.energystats.models.colour
import com.alpriest.energystats.shared.helpers.kW
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.ui.helpers.lightenColor
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent

enum class EnergyBreakdownType {
    Inputs,
    Outputs;

    fun title(context: Context, value: Double): String {
        return when (this) {
            Inputs -> context.getString(R.string.energy_breakdown_sources, value.kW(1))
            Outputs -> context.getString(R.string.energy_breakdown_uses, value.kW(1))
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
    val valuesAtTimeStream = viewModel.valuesAtTimeStream.collectAsStateWithLifecycle().value
    val modelProducer = remember { CartesianChartModelProducer() }
    val scrollState = rememberVicoScrollState(scrollEnabled = false)
    val zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content)
    val chartColors = (EnergyBreakdownType.Inputs.types + EnergyBreakdownType.Outputs.types).map { it.colour(appSettingsStream) }
    val totals = remember { mutableStateMapOf<EnergyBreakdownType, String>() }
    val context = LocalContext.current
    var modelReady by remember { mutableStateOf(false) }
    var hasRenderedOnce by rememberSaveable { mutableStateOf(false) }
    val hasData = totalsStream.isNotEmpty() || valuesAtTimeStream.isNotEmpty()

    LaunchedEffect(totalsStream, valuesAtTimeStream) {
        totals.clear()

        // If we have no data, hide the chart only if we've never shown it.
        if (!hasData) {
            if (!hasRenderedOnce) {
                modelReady = false
            }
            return@LaunchedEffect
        }

        // Only gate visibility on the very first render to avoid the initial wide-column layout.
        if (!hasRenderedOnce) {
            modelReady = false
        }

        modelProducer.runTransaction {
            if (valuesAtTimeStream.isNotEmpty()) {
                columnSeries {
                    EnergyBreakdownType.Inputs.types.forEach {
                        series(x = EnergyBreakdownType.Inputs.graphX, listOfNotNull(valuesAtTimeStream[it]?.firstOrNull()?.y))
                    }
                    EnergyBreakdownType.Outputs.types.forEach {
                        series(x = EnergyBreakdownType.Outputs.graphX, listOfNotNull(valuesAtTimeStream[it]?.firstOrNull()?.y))
                    }
                }

                totals[EnergyBreakdownType.Inputs] = valuesAtTimeStream.filter { EnergyBreakdownType.Inputs.types.contains(it.key) }
                    .values
                    .mapNotNull { it.firstOrNull()?.y }
                    .sum()
                    .run { EnergyBreakdownType.Inputs.title(context, this.toDouble()) }

                totals[EnergyBreakdownType.Outputs] = valuesAtTimeStream.filter { EnergyBreakdownType.Outputs.types.contains(it.key) }
                    .values
                    .mapNotNull { it.firstOrNull()?.y }
                    .sum()
                    .run { EnergyBreakdownType.Outputs.title(context, this.toDouble()) }

            } else if (totalsStream.isNotEmpty()) {
                columnSeries {
                    EnergyBreakdownType.Inputs.types.forEach {
                        series(x = EnergyBreakdownType.Inputs.graphX, listOfNotNull(totalsStream[it]))
                    }
                    EnergyBreakdownType.Outputs.types.forEach {
                        series(x = EnergyBreakdownType.Outputs.graphX, listOfNotNull(totalsStream[it]))
                    }
                }

                totals[EnergyBreakdownType.Inputs] = totalsStream.filter { EnergyBreakdownType.Inputs.types.contains(it.key) }
                    .values
                    .sum()
                    .run { EnergyBreakdownType.Inputs.title(context, this) }

                totals[EnergyBreakdownType.Outputs] = totalsStream.filter { EnergyBreakdownType.Outputs.types.contains(it.key) }
                    .values
                    .sum()
                    .run { EnergyBreakdownType.Outputs.title(context, this) }
            }
        }

        modelReady = true
        hasRenderedOnce = true
    }

    if (modelReady || hasRenderedOnce) {
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

private class EnergyBreakdownBottomAxisValueFormatter(private val totals: MutableMap<EnergyBreakdownType, String>) : CartesianValueFormatter {
    override fun format(
        context: CartesianMeasuringContext,
        value: Double,
        verticalAxisPosition: Axis.Position.Vertical?,
    ): CharSequence {
        val type = when (value) {
            EnergyBreakdownType.Inputs.graphX.first() -> EnergyBreakdownType.Inputs
            else -> EnergyBreakdownType.Outputs
        }

        return totals[type] ?: " "
    }
}

@Composable
private fun rememberColumnsLayer(chartColors: List<Color>): ColumnCartesianLayer {
    val lineColumnProvider = remember(chartColors) {
        ColumnCartesianLayer.ColumnProvider.series(
            *chartColors.map { color ->
                LineComponent(
                    Fill(
                        brush = Brush.verticalGradient(
                            listOf(
                                lightenColor(color, 0.2f),
                                color.copy(alpha = 1.0f)
                            ),
                            startY = 0.0f,
                            endY = 0.25f
                        )
                    ),
                    strokeThickness = 25.0.dp,
                    strokeFill = Fill(color.copy(alpha = 0.95f))
                )
            }.toTypedArray(),
        )
    }

    return rememberColumnCartesianLayer(
        lineColumnProvider,
        verticalAxisPosition = Axis.Position.Vertical.End,
        mergeMode = { ColumnCartesianLayer.MergeMode.Stacked }
    )
}