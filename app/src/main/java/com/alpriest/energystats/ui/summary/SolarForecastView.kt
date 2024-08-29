package com.alpriest.energystats.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.R
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.settings.dataloggers.Rectangle
import com.alpriest.energystats.ui.settings.solcast.SolarForecasting
import com.alpriest.energystats.ui.statsgraph.chartStyle
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Green
import com.alpriest.energystats.ui.theme.Red
import com.alpriest.energystats.ui.theme.TintColor
import com.alpriest.energystats.ui.theme.demo
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberEndAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.layout.fullWidth
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.MutableStateFlow

class SolarForecastView(
    private val solarForecastProvider: () -> SolarForecasting,
    val themeStream: MutableStateFlow<AppTheme>
) {
    private val predictionColor = TintColor
    private val color90 = Red.copy(alpha = 0.5f)
    private val color10 = Green.copy(alpha = 0.5f)

    @Composable
    fun Content(
        modifier: Modifier = Modifier,
        viewModel: SolarForecastViewModel = viewModel(factory = SolarForecastViewModelFactory(solarForecastProvider, this.themeStream))
    ) {
        val data = viewModel.dataStream.collectAsState().value
        val loadState: LoadState = viewModel.loadStateStream.collectAsState().value
        val context = LocalContext.current

        LaunchedEffect(null) {
            viewModel.load(context)
        }

        when (loadState) {
            is LoadState.Active -> LoadingView(loadState.value)
            is LoadState.Error -> {
                Column(
                    modifier = Modifier.padding(top = 22.dp)
                ) {
                    Text(
                        stringResource(R.string.solar_forecasts),
                        style = typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        loadState.reason,
                        color = colorScheme.onSecondary
                    )
                }
            }

            is LoadState.Inactive -> {
                Column(
                    modifier = modifier.fillMaxWidth().padding(top = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.solar_forecasts),
                        style = typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
                        data.map { site ->
                            ForecastView(site.today, site.todayTotal, site.name, stringResource(R.string.forecast_today), themeStream)
                            ForecastView(site.tomorrow, site.tomorrowTotal, site.name, stringResource(R.string.forecast_tomorrow), themeStream)
                        }
                    }

                    if (data.isEmpty()) {
                        Text(
                            stringResource(R.string.solar_forecasts_not_configured),
                            modifier = Modifier.fillMaxWidth(),
                            color = colorScheme.onSecondary
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 44.dp)
                        ) {
                            Rectangle(
                                color = predictionColor,
                                modifier = Modifier
                                    .size(width = 20.dp, height = 5.dp)
                                    .padding(end = 5.dp)
                            )
                            Text(
                                stringResource(R.string.prediction),
                                modifier = Modifier.padding(end = 15.dp),
                                style = TextStyle(color = colorScheme.onSecondary)
                            )

                            Rectangle(
                                color = color90,
                                modifier = Modifier
                                    .size(width = 20.dp, height = 5.dp)
                                    .padding(end = 5.dp)
                            )
                            Text(
                                stringResource(R.string.high_estimate),
                                modifier = Modifier.padding(end = 15.dp),
                                style = TextStyle(color = colorScheme.onSecondary)
                            )

                            Rectangle(
                                color = color10,
                                modifier = Modifier
                                    .size(width = 20.dp, height = 5.dp)
                                    .padding(end = 5.dp)
                            )
                            Text(
                                stringResource(R.string.low_estimate),
                                modifier = Modifier.padding(end = 15.dp),
                                style = TextStyle(color = colorScheme.onSecondary)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ForecastView(
        model: List<List<DateFloatEntry>>,
        todayTotal: Double,
        name: String?,
        title: String,
        themeStream: MutableStateFlow<AppTheme>
    ) {
        val theme = themeStream.collectAsState().value
        val chartColors = listOf(color90, color10, predictionColor)

        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                name?.let {
                    Text(
                        it,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(color = colorScheme.onSecondary)
                    )
                }

                Text(
                    title,
                    style = TextStyle(color = colorScheme.onSecondary)
                )

                Text(
                    todayTotal.energy(theme.displayUnit, theme.decimalPlaces),
                    style = TextStyle(color = colorScheme.onSecondary)
                )
            }

            ProvideChartStyle(chartStyle(chartColors, themeStream)) {
                Chart(
                    chart = lineChart(
                        axisValuesOverrider = AxisValuesOverrider.fixed(0f, 48f)
                    ),
                    chartModelProducer = ChartEntryModelProducer(model),
                    chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                    endAxis = rememberEndAxis(
                        itemPlacer = AxisItemPlacer.Vertical.default(5),
                        valueFormatter = DecimalFormatAxisValueFormatter("0.${"0".repeat(theme.decimalPlaces)} kW")
                    ),
                    bottomAxis = rememberBottomAxis(
                        itemPlacer = AxisItemPlacer.Horizontal.default(6, addExtremeLabelPadding = true),
                        label = axisLabelComponent(horizontalPadding = 2.dp),
                        valueFormatter = SolarGraphFormatAxisValueFormatter(),
                        guideline = null,
                    ),
                    horizontalLayout = HorizontalLayout.fullWidth()
                )
            }
        }
    }

    class SolarGraphFormatAxisValueFormatter<Position : AxisPosition> : AxisValueFormatter<Position> {
        override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
            return String.format("%d:%02d", (value.toInt() / 2), 0)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SolarForecastViewPreview() {
    EnergyStatsTheme {
        SolarForecastView(
            solarForecastProvider = { DemoSolarForecasting() },
            themeStream = MutableStateFlow(AppTheme.demo()),
        ).Content()
    }
}
