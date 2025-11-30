package com.alpriest.energystats.ui.paramsgraph

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.theme.AppTheme
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

@Composable
fun ParameterGraphViewVico2(
    producer: CartesianChartModelProducer,
    yAxisScale: AxisScale,
    chartColors: List<Color>,
    viewModel: ParametersGraphTabViewModel,
    themeStream: MutableStateFlow<AppTheme>,
    showYAxisUnit: Boolean,
    userManager: UserManaging
) {
    val entries = viewModel.entriesStream.collectAsState().value.firstOrNull() ?: listOf()
    val displayMode = viewModel.displayModeStream.collectAsState().value
    val bounds = viewModel.boundsStream.collectAsState().value
    val producers = viewModel.producers.collectAsState().value

    val max = (bounds.maxByOrNull { it.max }?.max) ?: 0f
    val min = (bounds.minByOrNull { it.min }?.min) ?: 0f
    val range = max - min
    val endAxisFormatter = if (showYAxisUnit) ParameterGraphEndAxisValueFormatterVico2(range) else CartesianValueFormatter.decimal(DecimalFormat("#.#"))
    val marker = ParameterGraphVerticalLineMarkerVico1(
        producers,
        viewModel.valuesAtTimeStream,
        viewModel.lastMarkerModelStream
    )
    val lastMarkerModel = viewModel.lastMarkerModelStream.collectAsState().value
    val truncatedYAxisOnParameterGraphs = themeStream.collectAsState().value.truncatedYAxisOnParameterGraphs

    MonitorAlertDialog(viewModel, userManager)

    if (entries.isNotEmpty()) {
        when (displayMode.hours) {
            24 -> ParameterGraphViewWithCustomMarkerVico2(
                producer,
                Modifier,
                chartColors,
                themeStream,
                endAxisFormatter,
                marker,
                lastMarkerModel,
                24,
                CartesianLayerRangeProvider.fixed(
                    minX = 0.0,
                    maxX = max(288.0, entries.count().toDouble()),
                    minY = if (truncatedYAxisOnParameterGraphs) yAxisScale.min?.toDouble() else null,
                    maxY = if (truncatedYAxisOnParameterGraphs) yAxisScale.max?.toDouble() else null
                )
            )

            6 -> ParameterGraphViewWithCustomMarkerVico2(
                producer,
                Modifier,
                chartColors,
                themeStream,
                endAxisFormatter,
                marker,
                lastMarkerModel,
                9,
                CartesianLayerRangeProvider.fixed(
                    minY = if (truncatedYAxisOnParameterGraphs) yAxisScale.min?.toDouble() else null,
                    maxY = if (truncatedYAxisOnParameterGraphs) yAxisScale.max?.toDouble() else null
                )
            )

            else -> ParameterGraphViewWithCustomMarkerVico2(
                producer,
                Modifier,
                chartColors,
                themeStream,
                endAxisFormatter,
                marker,
                lastMarkerModel,
                18,
                CartesianLayerRangeProvider.fixed(
                    minY = if (truncatedYAxisOnParameterGraphs) yAxisScale.min?.toDouble() else null,
                    maxY = if (truncatedYAxisOnParameterGraphs) yAxisScale.max?.toDouble() else null
                )
            )
        }
    }
}

@Composable
private fun ParameterGraphViewWithCustomMarkerVico2(
    producer: CartesianChartModelProducer,
    modifier: Modifier,
    chartColors: List<Color>,
    themeStream: MutableStateFlow<AppTheme>,
    endAxisFormatter: CartesianValueFormatter,
    marker: ParameterGraphVerticalLineMarkerVico1,
    lastMarkerModel: ParameterGraphVerticalLineMarkerModelVico1?,
    horizontalAxisSpacing: Int,
    rangeProvider: CartesianLayerRangeProvider
) {
    val truncatedYAxisOnParameterGraphs = themeStream.collectAsState().value.truncatedYAxisOnParameterGraphs

    // Simple bottom-axis formatter: show integer x values as-is.
    val bottomAxisFormatter = remember {
        CartesianValueFormatter { _, value, _ ->
            value.toInt().toString()
        }
    }

    // Build a line provider that applies your chartColors to each series.
    val lineProvider = LineCartesianLayer.LineProvider.series(
        *chartColors.map { color ->
            LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(fill(color))
            )
        }.toTypedArray()
    )

    val lineLayer = rememberLineCartesianLayer(
        lineProvider = lineProvider,
        rangeProvider = rangeProvider
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    lineLayer,
                    endAxis = VerticalAxis.rememberEnd(
                        itemPlacer = VerticalAxis.ItemPlacer.count(count = { 5 }),
                        valueFormatter = endAxisFormatter
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        itemPlacer = HorizontalAxis.ItemPlacer.aligned(
                            spacing = { horizontalAxisSpacing },
                            addExtremeLabelPadding = true
                        ),
                        valueFormatter = bottomAxisFormatter,
                        guideline = null
                    )
                ),
                modelProducer = producer,
                modifier = Modifier.fillMaxSize(),
                scrollState = rememberVicoScrollState(scrollEnabled = false),
                animateIn = false
            )

            // NOTE: custom markers and the SelectedParameterValuesLineMarkerVico2 overlay
            // are not yet implemented for Vico 2 here. The marker and lastMarkerModel
            // parameters are kept to preserve the call sites, but are currently unused.
        }
    }
}

//private class ParameterGraphBottomAxisValueFormatterVico2<Position : AxisPosition> : AxisValueFormatter<Position> {
//    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
//        return chartValues.chartEntryModel.entries
//            .asSequence()
//            .flatMap { it.asSequence() }
//            .firstOrNull { it.x == value }
//            ?.let { it as? DateTimeFloatEntryVico1 }
//            ?.localDateTime?.toLocalTime()
//            ?.run {
//                String.format(Locale.getDefault(), "%02d", hour)
//            }
//            .orEmpty()
//    }
//}

class ParameterGraphEndAxisValueFormatterVico2(private val range: Float) : CartesianValueFormatter {
    override fun format(context: CartesianMeasuringContext, value: Double, verticalAxisPosition: Axis.Position.Vertical?): CharSequence {
        val unit = context.model.extraStore.getOrNull(UnitKey)

        return if (unit == "%") {
            String.format(Locale.getDefault(), "%d %s", value.toInt(), "%")
        } else {
            if (abs(range.toDouble()) < 1.0) {
                String.format(Locale.getDefault(), "%.2f %s", value, unit)
            } else {
                String.format(Locale.getDefault(), "%.1f %s", value, unit)
            }
        }
    }
}
