package com.alpriest.energystats.ui.statsgraph

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alpriest.energystats.R
import com.alpriest.energystats.models.colour
import com.alpriest.energystats.shared.helpers.kW
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.ui.helpers.lightenColor
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
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider

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
    val modelProducer = remember { CartesianChartModelProducer() }
    val scrollState = rememberVicoScrollState(scrollEnabled = false)
    val zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content)
    val chartColors = (EnergyBreakdownType.Inputs.types + EnergyBreakdownType.Outputs.types).map { it.colour(appSettingsStream) }
    val totals = mutableMapOf<EnergyBreakdownType, String>()
    val context = LocalContext.current

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

        return totals[type] ?: "loading"
    }
}

@Composable
private fun rememberColumnsLayer(chartColors: List<Color>): ColumnCartesianLayer {
    val lineColumnProvider = remember(chartColors) {
        ColumnCartesianLayer.ColumnProvider.series(
            *chartColors.map { color ->
                LineComponent(
                    fill(
                        shaderProvider = ShaderProvider.verticalGradient(
                            intArrayOf(
                                lightenColor(color, 0.2f).toArgb(),
                                color.copy(alpha = 1.0f).toArgb()
                            ),
                            positions = floatArrayOf(0.0f, 0.25f),
                        )
                    ),
                    thicknessDp = 25.0f,
                    strokeFill = fill(color.copy(alpha = 0.95f))
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