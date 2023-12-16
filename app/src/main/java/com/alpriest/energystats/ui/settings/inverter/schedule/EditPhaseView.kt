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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.models.SchedulerModeResponse
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsColumnWithChildAndFooter
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.battery.TimePeriodView
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

data class EditPhaseErrorData(val minSOCError: String?,
                              val fdSOCError: String?)

class EditPhaseViewModel(
    val originalPhase: SchedulePhase,
    val onChange: (SchedulePhase) -> Unit,
    val onDelete: (String) -> Unit
) : ViewModel() {
    val modes = listOf(
        SchedulerModeResponse(color = "#00ff00", name = "Force charge", key = "ForceCharge"),
        SchedulerModeResponse(color = "#ff0000", name = "Force discharge", key = "ForceDischarge"),
        SchedulerModeResponse(color = "#ff0000", name = "Self Use", key = "SelfUse"),
    )
    val startTimeStream = MutableStateFlow(Time.now())
    val endTimeStream = MutableStateFlow(Time.now())
    val workModeStream = MutableStateFlow(modes.first())
    val forceDischargePowerStream = MutableStateFlow("0")
    val forceDischargeSOCStream = MutableStateFlow("0")
    val minSOCStream = MutableStateFlow("0")
    val errorStream = MutableStateFlow(EditPhaseErrorData(minSOCError = null, fdSOCError = null))

    init {
        viewModelScope.launch {
            startTimeStream.collect {
                notify()
            }
        }
    }

    private fun notify() {
        val phase = SchedulePhase.create(
            id = originalPhase.id,
            start = startTimeStream.value,
            end = endTimeStream.value,
            mode = workModeStream.value,
            forceDischargePower = forceDischargePowerStream.value.toIntOrNull() ?: 0,
            forceDischargeSOC = forceDischargeSOCStream.value.toIntOrNull() ?: 0,
            batterySOC = minSOCStream.value.toIntOrNull() ?: 0,
            color = Color.scheduleColor(workModeStream.value.key)
        )

        if (phase != null) {
            onChange(phase)
        }

        validate()
    }

    private fun validate() {
        var minSOCError: String? = null
        var fdSOCError: String? = null

        minSOCStream.value.toIntOrNull()?.let {
            if (it < 10 || it > 100) {
                minSOCError = "Please enter a number between 10 and 100"
            }
        }

        forceDischargeSOCStream.value.toIntOrNull()?.let {
            if (it < 10 || it > 100) {
                fdSOCError = "Please enter a number between 10 and 100"
            }
        }

        minSOCStream.value.toIntOrNull()?.let { soc ->
            forceDischargeSOCStream.value.toIntOrNull()?.let { fdSOC ->
                if (soc > fdSOC) {
                    minSOCError = "Min SoC must be less than or equal to Force Discharge SoC"
                }
            }
        }

        errorStream.value = EditPhaseErrorData(minSOCError, fdSOCError)
    }
}

@Composable
fun EditPhaseView(viewModel: EditPhaseViewModel = viewModel()) {
    val forceDischargeSOC = remember { mutableStateOf("10") }
    val startTime = viewModel.startTimeStream.collectAsState().value
    val endTime = viewModel.endTimeStream.collectAsState().value
    val workMode = viewModel.workModeStream.collectAsState().value

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

        MinSOCView(minSOC, workMode)

        ForceDischargeSOCView(forceDischargeSOC, workMode)

        Button(onClick = { viewModel.deletePhase() }) {
            Text("Delete phase")
        }
    }
}

@Composable
fun MinSOCView(minSOC: MutableState<String>, mode: SchedulerModeResponse) {
    val footerText = when (mode.key) {
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
                    value = minSOC.value,
                    onValueChange = { minSOC.value = it.filter { it.isDigit() } },
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
fun ForceDischargeSOCView(minSOC: MutableState<String>, mode: SchedulerModeResponse) {
    val footerText = when (mode.key) {
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
                    value = minSOC.value,
                    onValueChange = { minSOC.value = it.filter { it.isDigit() } },
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

@Preview(heightDp = 600, widthDp = 400)
@Composable
fun EditPhaseViewPreview() {
    EnergyStatsTheme {
        EditPhaseView()
    }
}