package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SchedulePhaseListItemView(phase: SchedulePhase, modifier: Modifier = Modifier) {
    val extra = when (phase.mode.key) {
        "ForceDischarge" -> " down to ${phase.forceDischargeSOC}% at ${phase.forceDischargePower}W"
        "ForceCharge" -> ""
        "SelfUse" -> " with ${phase.batterySOC}% min SOC"
        else -> ""
    }

    Row(
        modifier = modifier
            .padding(vertical = 8.dp)
            .height(IntrinsicSize.Max)
    ) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .fillMaxHeight()
                .background(phase.color)
        )

        Spacer(Modifier.width(8.dp))

        Column {
            Row {
                Text(phase.start.formatted(), color = MaterialTheme.colors.onSecondary, fontWeight = FontWeight.Bold)
                Text(" - ", color = MaterialTheme.colors.onSecondary, fontWeight = FontWeight.Bold)
                Text(phase.end.formatted(), color = MaterialTheme.colors.onSecondary, fontWeight = FontWeight.Bold)
            }

            Row {
                Text(phase.mode.name, color = MaterialTheme.colors.onSecondary.copy(alpha = 0.5f))
                Text(extra, color = MaterialTheme.colors.onSecondary.copy(alpha = 0.5f))
            }
        }
    }
}
