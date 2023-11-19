package com.alpriest.energystats.ui.summary

import androidx.compose.animation.core.SnapSpec
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SolarForecastView(viewModel: SolarForecastViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ProvideChartStyle {
            Chart(
                chart = columnChart(),
                chartModelProducer = viewModel.producer,
                chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
                startAxis = rememberStartAxis(
                    itemPlacer = AxisItemPlacer.Vertical.default(5),
                    valueFormatter = DecimalFormatAxisValueFormatter("0.0")
                ),
                bottomAxis = rememberBottomAxis(
                    itemPlacer = AxisItemPlacer.Horizontal.default(2),
                    label = axisLabelComponent(horizontalPadding = 2.dp),
                    valueFormatter = SolarGraphFormatAxisValueFormatter()
                ),
                diffAnimationSpec = SnapSpec()
            )
        }
    }
}

class SolarGraphFormatAxisValueFormatter<Position : AxisPosition> : AxisValueFormatter<Position> {
    override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
        return value.toInt().toString()
    }
}

@Preview(showBackground = true)
@Composable
fun SolarForecastViewPreview() {
    EnergyStatsTheme {
        SolarForecastView(viewModel = SolarForecastViewModel(solarForecastProvider = DemoSolarForecasting()))
    }
}

class DemoSolarForecasting : SolarForecasting {
    override suspend fun fetchForecast(): SolcastForecastResponseList {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

        return SolcastForecastResponseList(
            forecasts = listOf(
                SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T06:00:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T06:30:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T07:00:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T07:30:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0084, pvEstimate10 = 0.0056, pvEstimate90 = 0.0167, dateFormat.parse("2023-11-14T08:00:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0501, pvEstimate10 = 0.0223, pvEstimate90 = 0.0891, dateFormat.parse("2023-11-14T08:30:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0975, pvEstimate10 = 0.0418, pvEstimate90 = 0.1811, dateFormat.parse("2023-11-14T09:00:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.1635, pvEstimate10 = 0.0771, pvEstimate90 = 0.4012, dateFormat.parse("2023-11-14T09:30:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.3364, pvEstimate10 = 0.1377, pvEstimate90 = 0.746, dateFormat.parse("2023-11-14T10:00:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.4891, pvEstimate10 = 0.2125, pvEstimate90 = 1.1081, dateFormat.parse("2023-11-14T10:30:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.609, pvEstimate10 = 0.2531, pvEstimate90 = 1.505, dateFormat.parse("2023-11-14T11:00:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.7061, pvEstimate10 = 0.2835, pvEstimate90 = 1.8413, dateFormat.parse("2023-11-14T11:30:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.7667, pvEstimate10 = 0.2936, pvEstimate90 = 2.09, dateFormat.parse("2023-11-14T12:00:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.8404, pvEstimate10 = 0.3037, pvEstimate90 = 2.3005, dateFormat.parse("2023-11-14T12:30:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.9307, pvEstimate10 = 0.3138, pvEstimate90 = 2.5050, dateFormat.parse("2023-11-14T13:00:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.9832, pvEstimate10 = 0.3087, pvEstimate90 = 2.5392, dateFormat.parse("2023-11-14T13:30:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.9438, pvEstimate10 = 0.2733, pvEstimate90 = 2.5179, dateFormat.parse("2023-11-14T14:00:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.8035, pvEstimate10 = 0.1973, pvEstimate90 = 2.8682, dateFormat.parse("2023-11-14T14:30:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.5897, pvEstimate10 = 0.128, pvEstimate90 = 2.5599, dateFormat.parse("2023-11-14T15:00:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.1594, pvEstimate10 = 0.0716, pvEstimate90 = 1.6839, dateFormat.parse("2023-11-14T15:30:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0496, pvEstimate10 = 0.0248, pvEstimate90 = 0.6277, dateFormat.parse("2023-11-14T16:00:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0028, pvEstimate10 = 0.0028, pvEstimate90 = 0.0055, dateFormat.parse("2023-11-14T16:30:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T17:00:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T17:30:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T18:00:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T18:30:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T19:00:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T19:30:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T20:00:00Z")!!),
                SolcastForecastResponse(pvEstimate = 0.0, pvEstimate10 = 0.0, pvEstimate90 = 0.0, dateFormat.parse("2023-11-14T20:30:00Z")!!)
            )
        )
    }
}
