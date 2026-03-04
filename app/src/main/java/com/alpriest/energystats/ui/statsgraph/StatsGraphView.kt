package com.alpriest.energystats.ui.statsgraph

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.models.colour
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.shared.models.StatsTimeUsageGraphStyle
import com.alpriest.energystats.shared.models.demo
import com.alpriest.energystats.shared.models.isDarkMode
import com.alpriest.energystats.shared.network.DemoNetworking
import com.alpriest.energystats.ui.helpers.axisLabelColor
import com.alpriest.energystats.ui.statsgraph.StatsDisplayMode.Day
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
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import com.alpriest.energystats.shared.R as SharedR

@Composable
fun StatsGraphView(viewModel: StatsTabViewModel, modifier: Modifier = Modifier) {
    val displayMode = viewModel.displayModeStream.collectAsStateWithLifecycle().value
    val appSettingsStream = viewModel.appSettingsStream
    val viewData = viewModel.viewDataStateFlow.collectAsStateWithLifecycle().value
    val selectedValue = viewModel.selectedValueStream.collectAsStateWithLifecycle().value
    val chartColors = viewData.stats.keys.map { it.colour(appSettingsStream) }
    val selfSufficiencyGraphData = viewData.selfSufficiency
    val inverterConsumptionData = viewData.inverterUsage
    val batterySOCData = viewData.batterySOC
    val statsGraphData = viewData.stats.values
    val timeUsageGraphStyle = appSettingsStream.collectAsState().value.statsTimeUsageGraphStyle
    val modelProducer = remember(timeUsageGraphStyle) { CartesianChartModelProducer() }
    val bottomAxisFormatter = remember(displayMode) { BottomAxisValueFormatter(displayMode) }
    val scrollState = rememberVicoScrollState(scrollEnabled = false)
    val isSystemInDarkTheme = isDarkMode(appSettingsStream)
    val axisGuidelineColor = if (isSystemInDarkTheme) Color.DarkGray else Color.LightGray.copy(alpha = 0.5f)
    val zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content)
    val maxX = displayMode.xPlotCount

    LaunchedEffect(statsGraphData, timeUsageGraphStyle) {
        modelProducer.runTransaction {
            if (statsGraphData.any { it.isNotEmpty() }) {
                if (timeUsageGraphStyle == StatsTimeUsageGraphStyle.Line) {
                    lineSeries {
                        statsGraphData.forEach { seriesEntries: List<StatsChartEntry> ->
                            series(
                                x = seriesEntries.map { it.x },
                                y = seriesEntries.map { it.y.toDouble() }
                            )
                        }
                    }
                } else if (timeUsageGraphStyle == StatsTimeUsageGraphStyle.Bar) {
                    columnSeries {
                        statsGraphData.forEach { seriesEntries: List<StatsChartEntry> ->
                            series(
                                x = seriesEntries.map { it.x },
                                y = seriesEntries.map { it.y.toDouble() }
                            )
                        }
                    }
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

            lineSeries {
                series(
                    x = listOf(maxX.toFloat()),
                    y = listOf(0.0)
                )
            }
        }
    }

    if (statsGraphData.isEmpty() && inverterConsumptionData.isEmpty() && selfSufficiencyGraphData.isEmpty() && batterySOCData.isEmpty()) {
        Text(
            stringResource(R.string.no_data),
            color = MaterialTheme.colorScheme.onSecondary
        )
    } else {
        val selfSufficiencyColor = selfSufficiencyLineColor(isDarkMode(appSettingsStream))
        val selfSufficiencyLayer = rememberLineLayer(color = selfSufficiencyColor, verticalAxisPosition = Axis.Position.Vertical.Start)

        val inverterConsumptionColor = ReportVariable.InverterConsumption.colour(appSettingsStream)
        val inverterConsumptionLayer = rememberLineLayer(color = inverterConsumptionColor, verticalAxisPosition = Axis.Position.Vertical.End)

        val batterySOCColor = ReportVariable.BatterySOC.colour(appSettingsStream)
        val batterySOCLayer = rememberLineLayer(color = batterySOCColor, verticalAxisPosition = Axis.Position.Vertical.Start)

        val layers = buildList {
            if (statsGraphData.isNotEmpty() && timeUsageGraphStyle == StatsTimeUsageGraphStyle.Line) {
                add(rememberStatsLineLayer(chartColors))
            }
            if (statsGraphData.isNotEmpty() && timeUsageGraphStyle == StatsTimeUsageGraphStyle.Bar) {
                add(rememberStatsColumnLayer(chartColors))
            }
            if (selfSufficiencyGraphData.isNotEmpty()) add(selfSufficiencyLayer)
            if (inverterConsumptionData.isNotEmpty()) add(inverterConsumptionLayer)
            if (batterySOCData.isNotEmpty()) add(batterySOCLayer)

            add(rememberLineLayer(color = Color.Transparent, verticalAxisPosition = Axis.Position.Vertical.Start))
        }

        val textColor = axisLabelColor(isDarkMode(appSettingsStream))
        val graphLabel = rememberTextComponent(
            style = TextStyle.Default.copy(color = textColor, fontSize = 10.sp),
        )

        Column(modifier = modifier.fillMaxWidth()) {
            TimeSelectionText(viewModel)

            Box(modifier = Modifier.fillMaxSize()) {
                key(timeUsageGraphStyle) {
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            *layers.toTypedArray(),
                            endAxis = VerticalAxis.rememberEnd(
                                label = graphLabel,
                                itemPlacer = VerticalAxis.ItemPlacer.count(count = { 5 }),
                                valueFormatter = remember { CartesianValueFormatter.decimal(1) },
                                guideline = rememberAxisGuidelineComponent(fill = Fill(axisGuidelineColor))
                            ),
                            bottomAxis = HorizontalAxis.rememberBottom(
                                label = graphLabel,
                                itemPlacer = HorizontalAxis.ItemPlacer.aligned(
                                    spacing = { 2 }
                                ),
                                valueFormatter = bottomAxisFormatter,
                                guideline = null
                            ),
                            marker = remember {
                                StatsGraphLineMarker(viewModel.selectedValueStream)
                            },
                            markerController = CartesianMarkerController.rememberShowOnPress()
                        ),
                        modelProducer = modelProducer,
                        modifier = Modifier.height(200.dp),
                        scrollState = scrollState,
                        animateIn = false,
                        animationSpec = null,
                        zoomState = zoomState
                    )
                }

                selectedValue?.let {
                    SelectedStatsValuesLineMarker(
                        displayMode,
                        it,
                        appSettingsStream
                    )
                }
            }

            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(
                    when (displayMode) {
                        is Day -> stringResource(SharedR.string.hours)
                        is StatsDisplayMode.Month -> stringResource(R.string.days)
                        is StatsDisplayMode.Year -> stringResource(R.string.months)
                        is StatsDisplayMode.Custom -> {
                            when (displayMode.unit) {
                                CustomDateRangeDisplayUnit.DAYS -> stringResource(R.string.days)
                                CustomDateRangeDisplayUnit.MONTHS -> stringResource(R.string.months)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun StatsGraphViewPreview() {
    val application = LocalContext.current.applicationContext as Application

    val factory = StatsTabViewModelFactory(
        application,
        MutableStateFlow(Day(LocalDate.now())),
        FakeConfigManager(),
        DemoNetworking(),
        appSettingsStream = MutableStateFlow(AppSettings.demo()),
        { _, _ -> null }
    )
    val viewModel: StatsTabViewModel = viewModel(factory = factory)

    StatsGraphView(viewModel)
}

@Composable
private fun rememberStatsLineLayer(chartColors: List<Color>): LineCartesianLayer {
    val lineColumnProvider = remember(chartColors) {
        LineCartesianLayer.LineProvider.series(
            *chartColors.map { color ->
                LineCartesianLayer.Line(
                    LineCartesianLayer.LineFill.single(Fill(color))
                )
            }.toTypedArray()
        )
    }

    return rememberLineCartesianLayer(
        lineColumnProvider,
        verticalAxisPosition = Axis.Position.Vertical.End
    )
}

@Composable
private fun rememberStatsColumnLayer(chartColors: List<Color>): ColumnCartesianLayer {
    val columnColumnProvider = remember(chartColors) {
        ColumnCartesianLayer.ColumnProvider.series(
            *chartColors.map { color ->
                LineComponent(
                    Fill(color),
                    thickness = 4.dp,
                    strokeFill = Fill(color)
                )
            }.toTypedArray()
        )
    }

    return rememberColumnCartesianLayer(
        columnColumnProvider,
        verticalAxisPosition = Axis.Position.Vertical.End
    )
}

@Composable
private fun rememberLineLayer(
    color: Color,
    verticalAxisPosition: Axis.Position.Vertical
): LineCartesianLayer {
    val lineProvider = remember(color) {
        LineCartesianLayer.LineProvider.series(
            LineCartesianLayer.Line(
                LineCartesianLayer.LineFill.single(Fill(color))
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
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.MONTH, value.toInt() - 1)
                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                return monthFormat.format(calendar.time)
            }

            is StatsDisplayMode.Custom -> {
                when (displayMode.unit) {
                    CustomDateRangeDisplayUnit.DAYS -> displayMode.start.plusDays(value.toLong()).dayOfMonth.toString()
                    CustomDateRangeDisplayUnit.MONTHS -> {
                        // `value` is a 0-based month offset from `start` and can cross year boundaries.
                        val offset = value.toLong()
                        val date = displayMode.start.plusMonths(offset)
                        val formatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())
                        return date.format(formatter)
                    }
                }
            }
        }
    }
}