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
import androidx.compose.material.Button
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import androidx.navigation.NavHostController
import com.alpriest.energystats.ui.settings.ButtonLabels
import com.alpriest.energystats.ui.settings.ContentWithBottomButtonPair
import com.alpriest.energystats.ui.settings.ContentWithBottomButtons
import com.alpriest.energystats.ui.settings.ErrorTextView

data class EditPhaseErrorData(
    val minSOCError: String?,
    val fdSOCError: String?,
    val timeError: String?
)

@Composable
fun EditPhaseView(navController: NavHostController, viewModel: EditPhaseViewModel = viewModel(factory = EditPhaseViewModelFactory(navController))) {
    val context = LocalContext.current

    LaunchedEffect(null) {
        viewModel.load(context)
    }

    ContentWithBottomButtonPair(navController, onSave = { viewModel.save(context) }, { modifier ->
        SettingsPage(modifier) {
            TimeAndWorkModeView(viewModel)

            MinSOCView(viewModel)

            ForceDischargeSOCView(viewModel)

            ForceDischargePowerView(viewModel)

            Button(onClick = { viewModel.deletePhase() }) {
                Text(stringResource(R.string.delete_time_period))
            }
        }
    }, labels = ButtonLabels(context.getString(R.string.cancel), context.getString(R.string.apply)))
}

@Composable
fun TimeAndWorkModeView(viewModel: EditPhaseViewModel) {
    val startTime = viewModel.startTimeStream.collectAsState().value
    val endTime = viewModel.endTimeStream.collectAsState().value
    val errorText = viewModel.errorStream.collectAsState().value

    SettingsColumnWithChild {
        TimePeriodView(
            startTime, stringResource(R.string.start_time), labelStyle = TextStyle.Default,
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
                .padding(vertical = 14.dp)
        ) { hour, minute -> viewModel.startTimeStream.value = Time(hour, minute) }

        TimePeriodView(
            endTime, stringResource(R.string.end_time), labelStyle = TextStyle.Default,
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
                .padding(vertical = 14.dp)
        ) { hour, minute -> viewModel.endTimeStream.value = Time(hour, minute) }

        ErrorTextView(errorText.timeError)

        WorkModeView(viewModel)
    }
}

@Composable
fun MinSOCView(viewModel: EditPhaseViewModel) {
    val workMode = viewModel.workModeStream.collectAsState().value
    val minSOC = viewModel.minSOCStream.collectAsState().value
    val footerText = when (workMode) {
        WorkMode.ForceDischarge -> stringResource(R.string.force_discharge_timeperiod_minsoc_description)
        else -> null
    }
    val errorText = viewModel.errorStream.collectAsState().value

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
        error = errorText.minSOCError
    )
}

@Composable
fun ForceDischargeSOCView(viewModel: EditPhaseViewModel) {
    val workMode = viewModel.workModeStream.collectAsState().value
    val fdSOC = viewModel.forceDischargeSOCStream.collectAsState().value
    val footerText = when (workMode) {
        WorkMode.ForceDischarge -> stringResource(R.string.force_discharge_timeperiod_fdsoc_description)
        else -> null
    }
    val errorText = viewModel.errorStream.collectAsState().value

    SettingsColumnWithChildAndFooter(
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colors.surface)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    stringResource(R.string.force_discharge_soc),
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
        error = errorText.fdSOCError
    )
}

@Composable
fun ForceDischargePowerView(viewModel: EditPhaseViewModel) {
    val workMode = viewModel.workModeStream.collectAsState().value
    val fdPower = viewModel.forceDischargePowerStream.collectAsState().value
    val footerText = when (workMode) {
        WorkMode.ForceDischarge -> stringResource(R.string.force_discharge_timeperiod_power_description)
        else -> null
    }

    SettingsColumnWithChildAndFooter(
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colors.surface)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    stringResource(R.string.force_discharge_power),
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
        error = null
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
        Text(stringResource(R.string.work_mode))

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
        EditPhaseView(NavHostController(LocalContext.current))
    }
}

