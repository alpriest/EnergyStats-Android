package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.SchedulerModeResponse
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.battery.TimePeriodView
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun EditPhaseView() {
    var startTime by remember { mutableStateOf(Time.now()) }
    var endTime by remember { mutableStateOf(Time.now()) }
    val modes = listOf(
        SchedulerModeResponse(color = "#00ff00", name = "Force charge", key = "ForceCharge"),
        SchedulerModeResponse(color = "#ff0000", name = "Force discharge", key = "ForceDischarge"),
        SchedulerModeResponse(color = "#ff0000", name = "Self Use", key = "SelfUse"),
    )
    val mode = remember { mutableStateOf(modes.first()) }
    val minSOC = remember { mutableStateOf("10") }

    SettingsPage {
        SettingsColumnWithChild {
            TimePeriodView(
                startTime, "Start time", labelStyle = TextStyle.Default,
                modifier = Modifier
                    .background(MaterialTheme.colors.surface)
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            ) { hour, minute -> startTime = Time(hour, minute) }

            TimePeriodView(
                startTime, "End time", labelStyle = TextStyle.Default,
                modifier = Modifier
                    .background(MaterialTheme.colors.surface)
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            ) { hour, minute -> endTime = Time(hour, minute) }

            WorkModeView(mode, modes)
        }

        MinSOCView(minSOC, mode.value)
    }
}

@Composable
fun MinSOCView(minSOC: MutableState<String>, mode: SchedulerModeResponse) {
    SettingsColumnWithChild {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                stringResource(R.string.min_soc),
                Modifier.weight(1.0f),
                color = MaterialTheme.colors.onSecondary
            )
            OutlinedTextField(
                value = minSOC.value,
                onValueChange = { minSOC.value = it.filter { it.isDigit() } },
                modifier = Modifier.width(100.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = MaterialTheme.colors.onSecondary),
                trailingIcon = { Text("%", color = MaterialTheme.colors.onSecondary) }
            )
        }
    }
}

@Composable
fun WorkModeView(mode: MutableState<SchedulerModeResponse>, modes: List<SchedulerModeResponse>) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Text("Work mode")

        Box(contentAlignment = Alignment.TopEnd) {
            Button(onClick = { expanded = !expanded }) {
                Text(
                    mode.value.name,
                    color = MaterialTheme.colors.onPrimary
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onPrimary
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                modes.forEach { it ->
                    DropdownMenuItem(onClick = {
                        expanded = false
                        mode.value = it
                    }) {
                        Text(it.name)
                    }
                }
            }
        }
    }
}

@Preview(heightDp = 600, widthDp = 400)
@Composable
fun EditPhaseViewPreview() {
    EnergyStatsTheme {
        EditPhaseView()
    }
}