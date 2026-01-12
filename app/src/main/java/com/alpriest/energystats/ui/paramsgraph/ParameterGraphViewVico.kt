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
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.helpers.axisLabelColor
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.paramsgraph.graphs.AxisScale
import com.alpriest.energystats.ui.paramsgraph.graphs.VariableKey
import com.alpriest.energystats.shared.models.AppSettings
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
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
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.max

private const val SECONDS_IN_DAY = 86400

@Composable
fun ParameterGraphViewVico(
    producer: CartesianChartModelProducer,
    chartColors: List<Color>,
    yAxisScale: AxisScale,
    viewModel: ParametersGraphTabViewModel,
    themeStream: MutableStateFlow<AppSettings>,
    showYAxisUnit: Boolean,
    userManager: UserManaging,
    valuesAtTimeStream: List<DateTimeFloatEntry>
) {
    val entries = viewModel.entriesStream.collectAsState().value.firstOrNull() ?: listOf()
    val displayMode = viewModel.displayModeStream.collectAsState().value
    val bounds = viewModel.boundsStream.collectAsState().value

    val max = (bounds.maxByOrNull { it.max }?.max) ?: 0f
    val min = (bounds.minByOrNull { it.min }?.min) ?: 0f
    val range = max - min
    val endAxisFormatter = if (showYAxisUnit) ParameterGraphEndAxisValueFormatter(range) else CartesianValueFormatter.decimal(DecimalFormat("#.#"))
    val truncatedYAxisOnParameterGraphs = themeStream.collectAsState().value.truncatedYAxisOnParameterGraphs
    val startOfDay = displayMode.date.atStartOfDay().atZone(ZoneId.systemDefault()).toEpochSecond()

    MonitorAlertDialog(viewModel, userManager)

    if (entries.isNotEmpty()) {
        when (displayMode.hours) {
            24 -> ParameterGraphViewWithCustomMarker(
                producer,
                viewModel.selectedValueStream,
                valuesAtTimeStream,
                Modifier,
                chartColors,
                endAxisFormatter,
                CartesianLayerRangeProvider.fixed(
                    minX = startOfDay.toDouble(),
                    maxX = max(startOfDay + 86400.0, entries.count().toDouble()),
                    minY = if (truncatedYAxisOnParameterGraphs) yAxisScale.min?.toDouble() else null,
                    maxY = if (truncatedYAxisOnParameterGraphs) yAxisScale.max?.toDouble() else null
                ),
                themeStream
            )

            6 -> ParameterGraphViewWithCustomMarker(
                producer,
                viewModel.selectedValueStream,
                valuesAtTimeStream,
                Modifier,
                chartColors,
                endAxisFormatter,
                CartesianLayerRangeProvider.fixed(
                    minY = if (truncatedYAxisOnParameterGraphs) yAxisScale.min?.toDouble() else null,
                    maxY = if (truncatedYAxisOnParameterGraphs) yAxisScale.max?.toDouble() else null
                ),
                themeStream
            )

            else -> ParameterGraphViewWithCustomMarker(
                producer,
                viewModel.selectedValueStream,
                valuesAtTimeStream,
                Modifier,
                chartColors,
                endAxisFormatter,
                CartesianLayerRangeProvider.fixed(
                    minY = if (truncatedYAxisOnParameterGraphs) yAxisScale.min?.toDouble() else null,
                    maxY = if (truncatedYAxisOnParameterGraphs) yAxisScale.max?.toDouble() else null
                ),
                themeStream
            )
        }
    }
}

@Composable
private fun ParameterGraphViewWithCustomMarker(
    producer: CartesianChartModelProducer,
    selectedValueStream: MutableStateFlow<ParameterGraphLineMarkerModel?>,
    valuesAtTimeStream: List<DateTimeFloatEntry>,
    modifier: Modifier,
    chartColors: List<Color>,
    endAxisFormatter: CartesianValueFormatter,
    rangeProvider: CartesianLayerRangeProvider,
    themeStream: MutableStateFlow<AppSettings>
) {
    val bottomAxisFormatter = remember { BottomAxisValueFormatter }
    val selectedValue = selectedValueStream.collectAsState().value

    // Build a line provider that applies your chartColors to each series.
    val lineProvider = remember(chartColors) {
        LineCartesianLayer.LineProvider.series(
            *chartColors.map { color ->
                LineCartesianLayer.Line(
                    fill = LineCartesianLayer.LineFill.single(fill(color))
                )
            }.toTypedArray()
        )
    }

    val lineLayer = rememberLineCartesianLayer(
        lineProvider = lineProvider,
        rangeProvider = rangeProvider
    )

    val color = axisLabelColor(isDarkMode(themeStream))
    val graphLabel = rememberTextComponent(
        color = color,
        textSize = 10.sp,
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
                        label = graphLabel,
                        itemPlacer = VerticalAxis.ItemPlacer.count(count = { 5 }),
                        valueFormatter = endAxisFormatter
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        label = graphLabel,
                        itemPlacer = HorizontalAxis.ItemPlacer.aligned(
                            spacing = { SECONDS_IN_DAY / 24 },
                            addExtremeLabelPadding = true
                        ),
                        valueFormatter = bottomAxisFormatter,
                        guideline = null
                    ),
                    marker = remember {
                        ParameterGraphLineMarker(selectedValueStream)
                    }
                ),
                modelProducer = producer,
                modifier = Modifier.fillMaxSize(),
                scrollState = rememberVicoScrollState(scrollEnabled = false),
                animateIn = false,
                animationSpec = null
            )

            selectedValue?.let {
                ParameterValuesPopupVico(
                    valuesAtTimeStream,
                    it,
                    themeStream
                )
            }
        }
    }
}

private val BottomAxisValueFormatter =
    object : CartesianValueFormatter {
        private val dateFormat =
            SimpleDateFormat("H", Locale.ENGLISH).apply { timeZone = TimeZone.getDefault() }

        override fun format(
            context: CartesianMeasuringContext,
            value: Double,
            verticalAxisPosition: Axis.Position.Vertical?,
        ) = dateFormat.format(value.toLong() * 1000)
    }

private class ParameterGraphEndAxisValueFormatter(private val range: Float) : CartesianValueFormatter {
    override fun format(context: CartesianMeasuringContext, value: Double, verticalAxisPosition: Axis.Position.Vertical?): CharSequence {
        val unit = context.model.extraStore.getOrNull(VariableKey)?.unit

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
