package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsColumnWithChildAndFooter
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.battery.TimePeriodView
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import androidx.lifecycle.viewmodel.compose.viewModel

data class EditPhaseErrorData(
    val minSOCError: String?,
    val fdSOCError: String?
)

class EditPhaseView {
    @Composable
    fun Content(viewModel: EditPhaseViewModel = viewModel(factory = EditPhaseViewModelFactory())) {
        val startTime = viewModel.startTimeStream.collectAsState().value
        val endTime = viewModel.endTimeStream.collectAsState().value

        SettingsPage {
            SettingsColumnWithChild {
                TimePeriodView(
                    startTime, "Start time", labelStyle = TextStyle.Default,
                    modifier = Modifier
                        .background(MaterialTheme.colors.surface)
                        .padding(vertical = 14.dp)
                ) { hour, minute -> viewModel.startTimeStream.value = Time(hour, minute) }

                TimePeriodView(
                    endTime, "End time", labelStyle = TextStyle.Default,
                    modifier = Modifier
                        .background(MaterialTheme.colors.surface)
                        .padding(vertical = 14.dp)
                ) { hour, minute -> viewModel.endTimeStream.value = Time(hour, minute) }

                WorkModeView(viewModel)
            }

            MinSOCView(viewModel)

            ForceDischargeSOCView(viewModel)

            ForceDischargePowerView(viewModel)

            Button(onClick = { viewModel.deletePhase() }) {
                Text("Delete phase")
            }
        }
    }

    @Composable
    fun MinSOCView(viewModel: EditPhaseViewModel) {
        val workMode = viewModel.workModeStream.collectAsState().value
        val minSOC = viewModel.minSOCStream.collectAsState().value
        val footerText = when (workMode.key) {
            "ForceDischarge" -> "The minimum battery state of charge. This must be at most the Force Discharge SOC value."
            else -> null
        }
        val errorText = null

        SettingsColumnWithChildAndFooter(
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colors.surface)
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        stringResource(R.string.min_soc),
                        Modifier.weight(1.0f),
                        color = MaterialTheme.colors.onSecondary
                    )
                    OutlinedTextField(
                        value = minSOC,
                        onValueChange = { viewModel.minSOCStream.value = it.filter { it.isDigit() } },
                        modifier = Modifier.width(100.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = MaterialTheme.colors.onSecondary),
                        trailingIcon = { Text("%", color = MaterialTheme.colors.onSecondary) }
                    )
                }
            },
            footer = footerText,
            error = errorText
        )
    }

    @Composable
    fun ForceDischargeSOCView(viewModel: EditPhaseViewModel) {
        val workMode = viewModel.workModeStream.collectAsState().value
        val fdSOC = viewModel.forceDischargeSOCStream.collectAsState().value
        val footerText = when (workMode.key) {
            "ForceDischarge" -> "When the battery reaches this level, discharging will stop. If you wanted to save some battery power for later, perhaps set it to 50%."
            else -> null
        }
        val errorText = null

        SettingsColumnWithChildAndFooter(
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colors.surface)
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        "Force Discharge SoC",
                        Modifier.weight(1.0f),
                        color = MaterialTheme.colors.onSecondary
                    )
                    OutlinedTextField(
                        value = fdSOC,
                        onValueChange = { viewModel.forceDischargeSOCStream.value = it.filter { it.isDigit() } },
                        modifier = Modifier.width(100.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = MaterialTheme.colors.onSecondary),
                        trailingIcon = { Text("%", color = MaterialTheme.colors.onSecondary) }
                    )
                }
            },
            footer = footerText,
            error = errorText
        )
    }

    @Composable
    fun ForceDischargePowerView(viewModel: EditPhaseViewModel) {
        val workMode = viewModel.workModeStream.collectAsState().value
        val fdPower = viewModel.forceDischargePowerStream.collectAsState().value
        val footerText = when (workMode.key) {
            "ForceDischarge" -> "The output power level to be delivered, including your house load and grid export. E.g. If you have 5kW inverter then set this to 5000, then if the house load is 750W the other 4.25kW will be exported."
            else -> null
        }
        val errorText = null

        SettingsColumnWithChildAndFooter(
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colors.surface)
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        "Force Discharge SoC",
                        Modifier.weight(1.0f),
                        color = MaterialTheme.colors.onSecondary
                    )
                    OutlinedTextField(
                        value = fdPower,
                        onValueChange = { viewModel.forceDischargePowerStream.value = it.filter { it.isDigit() } },
                        modifier = Modifier.width(100.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = MaterialTheme.colors.onSecondary),
                        trailingIcon = { Text("%", color = MaterialTheme.colors.onSecondary) }
                    )
                }
            },
            footer = footerText,
            error = errorText
        )
    }

    @Composable
    fun WorkModeView(viewModel: EditPhaseViewModel) {
        var expanded by remember { mutableStateOf(false) }
        val workMode = viewModel.workModeStream.collectAsState().value

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Work mode")

            Box(contentAlignment = Alignment.TopEnd) {
                Button(onClick = { expanded = !expanded }) {
                    Text(
                        workMode.name,
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
                    viewModel.modes.forEach { it ->
                        DropdownMenuItem(onClick = {
                            expanded = false
                            viewModel.workModeStream.value = it
                        }) {
                            Text(it.name)
                        }
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
        EditPhaseView(originalPhase = SchedulePhase.preview(),
            onDelete = {},
            onChange = { }
        ).Content()
    }
}

