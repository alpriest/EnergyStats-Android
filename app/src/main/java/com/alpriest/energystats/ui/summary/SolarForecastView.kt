package com.alpriest.energystats.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alpriest.energystats.models.energy
import com.alpriest.energystats.ui.flow.home.preview
import com.alpriest.energystats.ui.settings.dataloggers.Rectangle
import com.alpriest.energystats.ui.settings.solcast.SolarForecasting
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Green
import com.alpriest.energystats.ui.theme.Red
import com.alpriest.energystats.ui.theme.TintColor
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import kotlinx.coroutines.flow.MutableStateFlow

class SolarForecastView(
    private val solarForecastProvider: SolarForecasting,
    val themeStream: MutableStateFlow<AppTheme>
) {
    private val predictionColor = TintColor
    private val color90 = Red.copy(alpha = 0.5f)
    private val color10 = Green.copy(alpha = 0.5f)

    @Composable
    fun Content(
        viewModel: SolarForecastViewModel = viewModel(factory = SolarForecastViewModelFactory(solarForecastProvider, this.themeStream)),
        modifier: Modifier = Modifier
    ) {
        val data = viewModel.dataStream.collectAsState().value

        LaunchedEffect(null) {
            viewModel.load()
        }

        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
                data.map { site ->
                    ForecastView(site.today.getModel(), site.todayTotal, site.name, "Forecast today", site.error, site.resourceId, themeStream)
                    ForecastView(site.tomorrow.getModel(), site.todayTotal, site.name, "Forecast tomorrow", site.error, site.resourceId, themeStream)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Rectangle(
                    color = predictionColor,
                    modifier = Modifier
                        .size(width = 20.dp, height = 5.dp)
                        .padding(end = 5.dp)

                )
                Text(
                    "Prediction",
                    modifier = Modifier.padding(end = 15.dp)
                )

                Rectangle(
                    color = color90,
                    modifier = Modifier
                        .size(width = 20.dp, height = 5.dp)
                        .padding(end = 5.dp)
                )
                Text(
                    "90%",
                    modifier = Modifier.padding(end = 15.dp)
                )

                Rectangle(
                    color = color10,
                    modifier = Modifier
                        .size(width = 20.dp, height = 5.dp)
                        .padding(end = 5.dp)

                )
                Text(
                    "10%",
                    modifier = Modifier.padding(end = 15.dp)
                )
            }
        }
    }

    @Composable
    fun ForecastView(model: ChartEntryModel, todayTotal: Double, name: String?, title: String, error: String?, resourceId: String, themeStream: MutableStateFlow<AppTheme>) {
        val theme = themeStream.collectAsState().value

        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                name?.let {
                    Text(
                        it,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(title)

                Text(todayTotal.energy(theme.displayUnit, theme.decimalPlaces))
            }

            ProvideChartStyle {
                Chart(
                    chart = lineChart(
                        lines = listOf(
                            LineChart.LineSpec(
                                lineColor = color90.toArgb()
                            ),
                            LineChart.LineSpec(
                                lineColor = color10.toArgb()
                            ),
                            LineChart.LineSpec(
                                lineColor = predictionColor.toArgb(),
                            )
                        )
                    ),
                    model = model,
                    chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                    startAxis = rememberStartAxis(
                        itemPlacer = AxisItemPlacer.Vertical.default(5),
                        valueFormatter = DecimalFormatAxisValueFormatter("0.0")
                    ),
                    bottomAxis = rememberBottomAxis(
                        itemPlacer = AxisItemPlacer.Horizontal.default(2),
                        label = axisLabelComponent(horizontalPadding = 2.dp),
                        valueFormatter = SolarGraphFormatAxisValueFormatter(),
                        guideline = null
                    )
                )
            }
        }
    }

    class SolarGraphFormatAxisValueFormatter<Position : AxisPosition> : AxisValueFormatter<Position> {
        override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
            return value.toInt().toString()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SolarForecastViewPreview() {
    EnergyStatsTheme {
        SolarForecastView(
            solarForecastProvider = DemoSolarForecasting(),
            themeStream = MutableStateFlow(AppTheme.preview()),
        ).Content()
    }
}
