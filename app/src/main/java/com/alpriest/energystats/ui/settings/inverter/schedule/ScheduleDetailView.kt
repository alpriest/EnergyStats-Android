package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild

@Composable
fun ScheduleDetailView(schedule: Schedule) {
    SettingsColumnWithChild {
        if (schedule.name.isNotEmpty()) {
            Text(schedule.name)
        }

        TimePeriodBarView(schedule.phases, modifier = Modifier.padding(bottom = 8.dp))
    }

    if (schedule.phases.isEmpty()) {
        Text("You have no time periods defined. Add a time period to define how you'd like your inverter to behave during specific hours.")
    }

    SettingsColumnWithChild(modifier = Modifier.fillMaxWidth()) {
        // TODO: NavigationLink to SchedulePhaseEditView for each item
        schedule.phases.forEach {
            SchedulePhaseListItemView(it, modifier = Modifier.fillMaxWidth())
        }
    }
}