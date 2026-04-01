package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.models.SchedulePhaseV3
import com.alpriest.energystats.shared.models.ScheduleV3
import com.alpriest.energystats.shared.models.WorkModes
import com.alpriest.energystats.shared.models.network.Time
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun ScheduleView(schedule: ScheduleV3, configManager: ConfigManaging, modifier: Modifier = Modifier) {
    Column(modifier) {
        TimePeriodBarView(schedule.phases, modifier = Modifier.padding(bottom = 8.dp))

        schedule.phases.forEach {
            SchedulePhaseListItemView(it, configManager)
        }
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
fun ScheduleViewPreview() {
    EnergyStatsTheme {
        ScheduleView(ScheduleV3.preview(), FakeConfigManager())
    }
}

internal fun ScheduleV3.Companion.preview(): ScheduleV3 {
    return ScheduleV3(
        name = "Summer running",
        phases = listOfNotNull(
            SchedulePhaseV3.create(
                start = Time(hour = 1, minute = 0),
                end = Time(hour = 2, minute = 0),
                mode = WorkModes.ForceCharge,
                extraParam = mapOf(
                    "fdPwr" to 0.0,
                    "fdSoc" to 100.0,
                    "minSoc" to 100.0
                )
            ),
            SchedulePhaseV3.create(
                start = Time(hour = 8, minute = 0),
                end = Time(hour = 14, minute = 30),
                mode = WorkModes.ForceDischarge,
                extraParam = mapOf(
                    "fdPwr" to 3500.0,
                    "fdSoc" to 20.0,
                    "minSoc" to 20.0
                )
            ),
            SchedulePhaseV3.create(
                start = Time(hour = 19, minute = 30),
                end = Time(hour = 23, minute = 30),
                mode = WorkModes.SelfUse,
                extraParam = mapOf(
                    "fdPwr" to 0.0,
                    "fdSoc" to 20.0,
                    "minSoc" to 20.0
                )
            )
        )
    )
}