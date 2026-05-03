package com.alpriest.energystats.ui.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.helpers.asPercent
import com.alpriest.energystats.shared.helpers.kWh
import com.alpriest.energystats.shared.ui.DimmedTextColor
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun GenerationVsForecastView(data: PercentageSolarForecastAchievedData, period: SolarForecastPeriod, onTogglePeriod: () -> Unit) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.solar_vs_forecast_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondary,
            )

            Spacer(Modifier.weight(1.0f))

            TextButton(
                onClick = onTogglePeriod
            ) {
                Text(if (period == SolarForecastPeriod.Yesterday) stringResource(R.string.yesterday) else stringResource(R.string._7_days))
            }
        }

        LabelledContent(stringResource(R.string.actual_generation), data.totalSolarAchieved.kWh(0))
        LabelledContent(stringResource(R.string.forecast_total), data.totalSolarForecast.kWh(0))
        LabelledContent(stringResource(R.string.forecast_completeness)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PercentageBar(
                    percentage = data.forecastCompleteness,
                    modifier = Modifier.width(100.dp)
                )

                Spacer(Modifier.width(4.dp))

                Text(data.forecastCompleteness.asPercent())
            }
        }

        Text(
            data.description,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 12.dp),
            color = DimmedTextColor
        )
    }
}

@Composable
fun LabelledContent(title: String, content: String) {
    LabelledContent(title) {
        Text(
            content,
            color = colorScheme.onSecondary
        )
    }
}

@Composable
fun LabelledContent(title: String, content: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(end = 8.dp)
    ) {
        Text(
            title,
            color = colorScheme.onSecondary
        )
        content()
    }
}

@Composable
fun PercentageBar(
    percentage: Double,
    modifier: Modifier = Modifier
) {
    val clamped = percentage.coerceIn(0.0, 1.0)

    Box(
        modifier = modifier
            .height(14.dp)
    ) {
        // Background
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray.copy(alpha = 0.2f))
        )

        // Progress
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(2.dp)
                .fillMaxWidth(clamped.toFloat())
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Blue)
        )

        // Border
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
        )
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 600)
@Composable
fun GenerationVsForecastViewPreview() {
    EnergyStatsTheme {
        GenerationVsForecastView(
            data = PercentageSolarForecastAchievedData(
                totalSolarForecast = 50.0,
                totalSolarAchieved = 54.0,
                percentageSolarForecastAchieved = 1.08,
                description = "$1 of required Solcast data is available. $2 of forecast generated.",
                forecastCompleteness = 0.73
            ),
            period = SolarForecastPeriod.Yesterday,
            onTogglePeriod = {}
        )
    }
}