package com.alpriest.energystats.ui.settings.inverter.schedule

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPaddingValues
import com.alpriest.energystats.ui.settings.SettingsScreen
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun ScheduleDetailView(navController: NavHostController, schedule: Schedule) {
    SettingsColumn(
        padding = SettingsPaddingValues.default()
    ) {
        if (schedule.name.isNotEmpty()) {
            Column(modifier = Modifier.padding(PaddingValues(top = 10.dp, bottom = 8.dp))) {
                Text(schedule.name, color = colorScheme.onSecondary)
            }
        } else {
            Spacer(modifier = Modifier.padding(PaddingValues(top = 10.dp, bottom = 8.dp)))
        }

        TimePeriodBarView(schedule.phases, modifier = Modifier.padding(bottom = 8.dp))
    }

    if (schedule.phases.isEmpty()) {
        Text(
            stringResource(R.string.no_schedule_time_periods),
            color = colorScheme.onSecondary
        )
    }

    if (schedule.phases.isNotEmpty()) {
        SettingsColumnWithChild(modifier = Modifier.fillMaxWidth()) {
            schedule.phases.forEach {
                OutlinedButton(
                    onClick = {
                        EditScheduleStore.shared.scheduleStream.value = schedule
                        EditScheduleStore.shared.phaseId = it.id
                        navController.navigate(SettingsScreen.EditPhase.name)
                    },
                    border = null,
                    shape = RectangleShape,
                    contentPadding = PaddingValues()
                ) {
                    SchedulePhaseListItemView(
                        it,
                        modifier = Modifier
                    )

                    Spacer(modifier = Modifier.weight(0.1f))

                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Edit")
                }

                if (schedule.phases.last() != it) {
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
