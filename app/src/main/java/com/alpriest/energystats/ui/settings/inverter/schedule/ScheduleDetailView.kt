package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.models.ColorThemeMode
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsPadding
import com.alpriest.energystats.ui.settings.SettingsScreen
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.shared.models.Schedule

@Composable
fun ScheduleDetailView(navController: NavHostController, schedule: Schedule) {
    SettingsColumn(padding = PaddingValues()) {
        if (schedule.name.isNotEmpty()) {
            Column(modifier = Modifier.padding(PaddingValues(top = 10.dp, bottom = 8.dp))) {
                Text(
                    schedule.name,
                    color = colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Spacer(modifier = Modifier.padding(PaddingValues(top = 10.dp, bottom = 8.dp)))
        }

        TimePeriodBarView(schedule.phases, modifier = Modifier.padding(bottom = 8.dp))
    }

    if (schedule.phases.isEmpty()) {
        Text(
            stringResource(R.string.no_schedule_time_periods),
            color = colorScheme.onSecondary,
            modifier = Modifier.padding(
                horizontal = SettingsPadding.PANEL_OUTER_HORIZONTAL
            )
        )
    }

    if (schedule.phases.isNotEmpty()) {
        SettingsColumn(modifier = Modifier.fillMaxWidth(), padding = PaddingValues()) {
            schedule.phases.forEachIndexed { index, phase ->
                Box(Modifier.diagonalLinesIf(index >= Schedule.MAX_PHASES_COUNT)) {
                    OutlinedButton(
                        onClick = {
                            EditScheduleStore.shared.scheduleStream.value = schedule
                            EditScheduleStore.shared.phaseId = phase.id
                            navController.navigate(SettingsScreen.EditPhase.name)
                        },
                        border = null,
                        shape = RectangleShape,
                        contentPadding = PaddingValues()
                    ) {
                        SchedulePhaseListItemView(
                            phase,
                            modifier = Modifier
                        )

                        Spacer(modifier = Modifier.weight(0.1f))

                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Edit")
                    }
                }

                if (index != schedule.phases.size - 1) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleDetailViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        ScheduleDetailView(
            navController = NavHostController(LocalContext.current),
            schedule = Schedule.preview()
        )
    }
}
