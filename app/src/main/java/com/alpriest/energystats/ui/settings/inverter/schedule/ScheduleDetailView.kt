package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.demo
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ScheduleDetailView(title: String, navController: NavHostController, schedule: Schedule) {
    SettingsColumn(
        padding = SettingsPaddingValues.default()
    ) {
        if (schedule.name.isNotEmpty()) {
            Column(modifier = Modifier.padding(PaddingValues(top = 10.dp, bottom = 8.dp))) {
                Text(schedule.name, color = colors.onSecondary)
            }
        } else {
            Spacer(modifier = Modifier.padding(PaddingValues(top = 10.dp, bottom = 8.dp)))
        }

        TimePeriodBarView(schedule.phases, modifier = Modifier.padding(bottom = 8.dp))
    }

    if (schedule.phases.isEmpty()) {
        Text(
            stringResource(R.string.no_schedule_time_periods),
            color = colors.onSecondary
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
                    Divider()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleDetailViewPreview() {
    val themeStream = MutableStateFlow(AppTheme.demo())
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        ScheduleDetailView(
            title = "Something",
            navController = NavHostController(LocalContext.current),
            schedule = Schedule.preview()
        )
    }
}
