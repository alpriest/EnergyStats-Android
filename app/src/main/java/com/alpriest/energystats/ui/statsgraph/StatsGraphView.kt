package com.alpriest.energystats.ui.statsgraph

import android.R.attr.data
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alpriest.energystats.R
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.statsgraph.StatsDisplayMode.Day
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.demo
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.component.LineComponent
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun StatsGraphView(viewModel: StatsTabViewModel, modifier: Modifier = Modifier) {
    val displayMode = viewModel.displayModeStream.collectAsStateWithLifecycle().value
    val themeStream = viewModel.themeStream
    val viewData = viewModel.viewDataStateFlow.collectAsStateWithLifecycle().value
    val selectedValue = viewModel.selectedValueStream.collectAsStateWithLifecycle().value
    val chartColors = viewData.stats.keys.map { it.colour(themeStream) }
    val selfSufficiencyGraphData = viewData.selfSufficiency
    val inverterConsumptionData = viewData.inverterUsage
    val batterySOCData = viewData.batterySOC
    val statsGraphData = viewData.stats.values
    val modelProducer = remember { CartesianChartModelProducer() }
    val bottomAxisFormatter = remember(displayMode) { BottomAxisValueFormatter(displayMode) }
    val scrollState = rememberVicoScrollState(scrollEnabled = false)
    val isSystemInDarkTheme = isDarkMode(themeStream)
    val axisGuidelineColor = if (isSystemInDarkTheme) Color.DarkGray else Color.LightGray.copy(alpha = 0.5f)

    LaunchedEffect(statsGraphData) {
        if (statsGraphData.any { it.isNotEmpty() }) {
            modelProducer.runTransaction {
                columnSeries {
                    statsGraphData.forEach { seriesEntries: List<StatsChartEntry> ->
                        series(
                            x = seriesEntries.map { it.x },
                            y = seriesEntries.map { it.y.toDouble() }
                        )
                    }
                }

                if (selfSufficiencyGraphData.isNotEmpty()) {
                    lineSeries {
                        series(
                            x = selfSufficiencyGraphData.map { it.x },
                            y = selfSufficiencyGraphData.map { it.y.toDouble() }
                        )
                    }
                }

                if (inverterConsumptionData.isNotEmpty()) {
                    lineSeries {
                        series(
                            x = inverterConsumptionData.map { it.x },
                            y = inverterConsumptionData.map { it.y.toDouble() }
                        )
                    }
                }

                if (batterySOCData.isNotEmpty()) {
                    lineSeries {
                        series(
                            x = batterySOCData.map { it.x },
                            y = batterySOCData.map { it.y.toDouble() }
                        )
                    }
                }
            }
        }
    }

    if (statsGraphData.isEmpty() && inverterConsumptionData.isEmpty() && selfSufficiencyGraphData.isEmpty()) {
        Text(
            stringResource(R.string.no_data),
            color = MaterialTheme.colorScheme.onSecondary
        )
    } else {
        val statsLayer = rememberStatsLayer(chartColors)

        val selfSufficiencyColor = selfSufficiencyLineColor(isDarkMode(themeStream))
        val selfSufficiencyLayer = rememberLineLayer(color = selfSufficiencyColor, verticalAxisPosition = Axis.Position.Vertical.Start)

        val inverterConsumptionColor = ReportVariable.InverterConsumption.colour(themeStream)
        val inverterConsumptionLayer = rememberLineLayer(color = inverterConsumptionColor, verticalAxisPosition = Axis.Position.Vertical.End)

        val batterySOCColor = ReportVariable.BatterySOC.colour(themeStream)
        val batterySOCLayer = rememberLineLayer(color = batterySOCColor, verticalAxisPosition = Axis.Position.Vertical.Start)

        val layers = buildList {
            if (statsGraphData.isNotEmpty()) add(statsLayer)
            if (selfSufficiencyGraphData.isNotEmpty()) add(selfSufficiencyLayer)
            if (inverterConsumptionData.isNotEmpty()) add(inverterConsumptionLayer)
            if (batterySOCData.isNotEmpty()) add(batterySOCLayer)
        }

        val graphLabel = rememberTextComponent(
            color = vicoTheme.textColor,
            textSize = 10.sp,
        )

        Column(modifier = modifier.fillMaxWidth()) {
            TimeSelectionText(viewModel)

            Box(modifier = Modifier.fillMaxSize()) {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        *layers.toTypedArray(),
                        endAxis = VerticalAxis.rememberEnd(
                            label = graphLabel,
                            itemPlacer = VerticalAxis.ItemPlacer.count(count = { 5 }),
                            valueFormatter = remember { CartesianValueFormatter.decimal(DecimalFormat("#.#")) },
                            guideline = rememberAxisGuidelineComponent(fill = fill(axisGuidelineColor))
                        ),
                        bottomAxis = HorizontalAxis.rememberBottom(
                            label = graphLabel,
                            itemPlacer = HorizontalAxis.ItemPlacer.aligned(
                                spacing = { 3 },
                            ),
                            valueFormatter = bottomAxisFormatter,
                            guideline = null
                        ),
                        marker = remember {
                            StatsGraphLineMarker(viewModel.selectedValueStream)
                        }
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier.fillMaxSize(),
                    scrollState = scrollState,
                    animateIn = false,
                    animationSpec = null
                )

                selectedValue?.let {
                    SelectedStatsValuesLineMarker(displayMode.segmentCount, it, themeStream)
                }
            }

            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(
                    when (displayMode) {
                        is Day -> stringResource(R.string.hours)
                        is StatsDisplayMode.Month -> stringResource(R.string.days)
                        is StatsDisplayMode.Year -> stringResource(R.string.months)
                        is StatsDisplayMode.Custom -> stringResource(R.string.days)
                    }
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun StatsGraphViewPreview() {
    val viewModel = StatsTabViewModel(FakeConfigManager(), DemoNetworking(), themeStream = MutableStateFlow(AppTheme.demo()), onWriteTempFile = { _, _ -> null })

    StatsGraphView(viewModel)
}

@Composable
private fun rememberStatsLayer(chartColors: List<Color>): ColumnCartesianLayer {
    val statsColumnProvider = remember(chartColors) {
        ColumnCartesianLayer.ColumnProvider.series(
            *chartColors.map { color ->
                LineComponent(
                    fill(color),
                    thicknessDp = 8.0f,
                    strokeFill = fill(color)
                )
            }.toTypedArray()
        )
    }

    return rememberColumnCartesianLayer(statsColumnProvider)
}

@Composable
private fun rememberLineLayer(
    color: Color,
    verticalAxisPosition: Axis.Position.Vertical
): LineCartesianLayer {
    val lineProvider = remember(color) {
        LineCartesianLayer.LineProvider.series(
            LineCartesianLayer.Line(
                LineCartesianLayer.LineFill.single(fill(color))
            )
        )
    }

    return rememberLineCartesianLayer(
        lineProvider,
        verticalAxisPosition = verticalAxisPosition
    )
}

class BottomAxisValueFormatter(private val displayMode: StatsDisplayMode) : CartesianValueFormatter {
    override fun format(context: CartesianMeasuringContext, value: Double, verticalAxisPosition: Axis.Position.Vertical?): CharSequence {
        return when (displayMode) {
            is Day -> String.format(Locale.getDefault(), "%d:00", value.toInt())
            is StatsDisplayMode.Month -> value.toInt().toString()
            is StatsDisplayMode.Year -> {
                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.MONTH, value.toInt() - 1)
                return monthFormat.format(calendar.time)
            }

            is StatsDisplayMode.Custom -> displayMode.start.plusDays(value.toLong()).dayOfMonth.toString()
        }
    }
}