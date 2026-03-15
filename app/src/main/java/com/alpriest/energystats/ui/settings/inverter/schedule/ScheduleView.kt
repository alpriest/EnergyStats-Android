package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.shared.models.Schedule
import com.alpriest.energystats.shared.models.SchedulePhase
import com.alpriest.energystats.shared.models.WorkModes
import com.alpriest.energystats.shared.models.network.Time
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

sealed interface PhaseEnabledToggleMode {
    val isEnabled: Boolean

    data object Disabled : PhaseEnabledToggleMode {
        override val isEnabled = false
    }

    class Enabled(
        val onPhaseEnabledChange: (SchedulePhase, Boolean) -> Unit
    ) : PhaseEnabledToggleMode {
        override val isEnabled = true
    }

    fun onChange(phase: SchedulePhase, value: Boolean) {
        if (this is Enabled) {
            onPhaseEnabledChange(phase, value)
        }
    }
}

@Composable
fun ScheduleView(schedule: Schedule, toggleMode: PhaseEnabledToggleMode, modifier: Modifier = Modifier) {
    Column(modifier) {
        TimePeriodBarView(schedule.phases, modifier = Modifier.padding(bottom = 8.dp))

        schedule.phases.forEach {
            SchedulePhaseListItemView(it, toggleMode)
        }
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
fun ScheduleViewPreview() {
    EnergyStatsTheme {
        ScheduleView(Schedule.preview(), toggleMode = PhaseEnabledToggleMode.Enabled({ _, _ -> }))
    }
}

internal fun Schedule.Companion.preview(): Schedule {
    return Schedule(
        name = "Summer running",
        phases = listOfNotNull(
            SchedulePhase.create(
                enabled = true,
                start = Time(hour = 1, minute = 0),
                end = Time(hour = 2, minute = 0),
                mode = WorkModes.ForceCharge,
                forceDischargePower = 0,
                forceDischargeSOC = 100,
                batterySOC = 100,
                maxSOC = 100
            ),
            SchedulePhase.create(
                enabled = false,
                start = Time(hour = 8, minute = 0),
                end = Time(hour = 14, minute = 30),
                mode = WorkModes.ForceDischarge,
                forceDischargePower = 3500,
                forceDischargeSOC = 20,
                batterySOC = 20,
                maxSOC = 100
            ),
            SchedulePhase.create(
                enabled = true,
                start = Time(hour = 19, minute = 30),
                end = Time(hour = 23, minute = 30),
                mode = WorkModes.SelfUse,
                forceDischargePower = 0,
                forceDischargeSOC = 20,
                batterySOC = 20,
                maxSOC = 100
            )
        )
    )
}