package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild

@Composable
fun ScheduleDetailView(navController: NavHostController, schedule: Schedule) {
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
        schedule.phases.forEach {
            OutlinedButton(
                onClick = {
                    EditScheduleStore.shared.phaseId = it.id
                    navController.navigate(ScheduleScreen.EditPhase.name)

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
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier.clickable {
//                    EditScheduleStore.shared.phaseId = it.id
//                    navController.navigate(ScheduleScreen.EditPhase.name)
//                }
//            ) {
//                SchedulePhaseListItemView(
//                    it,
//                    modifier = Modifier
//                )
//
//                Spacer(modifier = Modifier.weight(0.1f))
//
//                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Edit")
//            }

            if (schedule.phases.last() != it) {
                Divider()
            }
        }
    }
}
