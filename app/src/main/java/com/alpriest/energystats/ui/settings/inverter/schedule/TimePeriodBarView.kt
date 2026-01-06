package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.shared.models.Schedule
import com.alpriest.energystats.shared.models.SchedulePhase
import com.alpriest.energystats.shared.models.ColorThemeMode
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.Orange

@Composable
fun TimePeriodBarView(phases: List<SchedulePhase>, modifier: Modifier = Modifier) {
    val height = 20.dp

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(Orange.copy(alpha = 0.1f))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                phases.forEach { phase ->
                    drawRect(
                        color = Color.scheduleColor(phase.mode),
                        size = Size(width * (phase.endPoint - phase.startPoint), height.toPx()),
                        topLeft = Offset(x = width * phase.startPoint, y = 0f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "00:00", fontSize = 12.sp, color = colorScheme.onSecondary)
            Text(text = "08:00", fontSize = 12.sp, color = colorScheme.onSecondary)
            Text(text = "16:00", fontSize = 12.sp, color = colorScheme.onSecondary)
            Text(text = "23:59", fontSize = 12.sp, color = colorScheme.onSecondary)
        }

    }
}

@Preview(showBackground = true)
@Composable
fun TimePeriodBarViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        TimePeriodBarView(Schedule.preview().phases)
    }
}