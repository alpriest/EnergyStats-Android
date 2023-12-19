package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.SchedulerModeResponse
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun ScheduleView(schedule: Schedule, modifier: Modifier = Modifier) {
    Column(modifier) {
        TimePeriodBarView(schedule.phases, modifier = Modifier.padding(bottom = 8.dp))

        Column {
            schedule.phases.forEach {
                SchedulePhaseListItemView(it)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
fun ScheduleViewPreview() {
    EnergyStatsTheme {
        ScheduleView(Schedule.preview())
    }
}

internal fun Schedule.Companion.preview(): Schedule {
    return Schedule(
        name = "Summer running",
        phases = listOfNotNull(
            SchedulePhase.create(
                start = Time(hour = 1, minute = 0),
                end = Time(hour = 2, minute = 0),
                mode = SchedulerModeResponse(color = "#00ff00", name = "Force charge", key = "ForceCharge"),
                forceDischargePower = 0,
                forceDischargeSOC = 100,
                batterySOC = 100,
                color = Color.scheduleColor("ForceCharge")
            ),
            SchedulePhase.create(
                start = Time(hour = 10, minute = 30),
                end = Time(hour = 14, minute = 30),
                mode = SchedulerModeResponse(color = "#ff0000", name = "Force discharge", key = "ForceDischarge"),
                forceDischargePower = 3500,
                forceDischargeSOC = 20,
                batterySOC = 20,
                color = Color.scheduleColor("ForceDischarge")
            ),
            SchedulePhase.create(
                start = Time(hour = 19, minute = 30),
                end = Time(hour = 23, minute = 30),
                mode = SchedulerModeResponse(color = "#ff0000", name = "Self Use", key = "SelfUse"),
                forceDischargePower = 0,
                forceDischargeSOC = 20,
                batterySOC = 20,
                color = Color.scheduleColor("SelfUse")
            )
        ),
        description = null
    )
}