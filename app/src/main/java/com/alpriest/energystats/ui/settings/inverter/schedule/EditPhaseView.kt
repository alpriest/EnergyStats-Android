package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Time
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.ButtonLabels
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.ContentWithBottomButtonPair
import com.alpriest.energystats.ui.settings.ErrorTextView
import com.alpriest.energystats.ui.settings.SettingsBottomSpace
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.battery.TimePeriodView
import com.alpriest.energystats.ui.settings.battery.TimeType
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.PaleWhite
import com.alpriest.energystats.ui.theme.PowerFlowNegative

data class EditPhaseErrorData(
    val minSOCError: String?,
    val fdSOCError: String?,
    val timeError: String?,
    val forceDischargePowerError: String?,
    val maxSOCError: String?
)

@Composable
fun EditPhaseView(
    navController: NavHostController,
    userManager: UserManaging,
    configManager: ConfigManaging,
    viewModel: EditPhaseViewModel = viewModel(factory = EditPhaseViewModelFactory(navController, configManager)),
    modifier: Modifier
) {
    val context = LocalContext.current
    val showMaxSoc = viewModel.showMaxSocStream.collectAsState().value

    LaunchedEffect(null) {
        viewModel.load(context)
    }
    trackScreenView("Edit phase", "EditPhaseView")

    ContentWithBottomButtonPair(
        navController,
        onConfirm = { viewModel.save(context) }, { innerModifier ->
            SettingsPage(innerModifier) {
                TimeAndWorkModeView(viewModel, userManager)

                MinSOCView(viewModel)

                if (showMaxSoc) {
                    MaxSOCView(viewModel)
                }

                ForceDischargeSOCView(viewModel)

                ForceDischargePowerView(viewModel)

                ESButton(
                    onClick = { viewModel.deletePhase() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PowerFlowNegative,
                        contentColor = PaleWhite
                    ),
                ) {
                    Text(stringResource(R.string.delete_time_period))
                }

                SettingsBottomSpace()
            }
        },
        modifier = modifier,
        labels = ButtonLabels(context.getString(R.string.cancel), context.getString(R.string.apply))
    )
}

@Composable
fun TimeAndWorkModeView(viewModel: EditPhaseViewModel, userManager: UserManaging) {
    val startTime = viewModel.startTimeStream.collectAsState().value
    val endTime = viewModel.endTimeStream.collectAsState().value
    val errorText = viewModel.errorStream.collectAsState().value
    val footerText = when (viewModel.workModeStream.collectAsState().value) {
        WorkMode.SelfUse -> stringResource(R.string.workmode_self_use_description)
        WorkMode.Feedin -> stringResource(R.string.workmode_feed_in_first_description)
        WorkMode.Backup -> stringResource(R.string.workmode_backup_description)
        WorkMode.ForceCharge -> stringResource(R.string.workmode_force_charge_description)
        WorkMode.ForceDischarge -> stringResource(R.string.workmode_force_discharge_description)
        else -> null
    }

    MonitorAlertDialog(viewModel = viewModel, userManager = userManager)

    SettingsColumnWithChild(
        footerAnnotatedString = buildAnnotatedString {
            footerText?.let {
                append(it)
            }
        }
    ) {
        TimePeriodView(
            startTime,
            TimeType.START,
            stringResource(R.string.start_time),
            labelStyle = TextStyle.Default,
            modifier = Modifier
                .background(colorScheme.surface)
                .padding(vertical = 14.dp)
        ) { hour, minute -> viewModel.startTimeStream.value = Time(hour, minute) }

        TimePeriodView(
            endTime,
            TimeType.END,
            stringResource(R.string.end_time),
            labelStyle = TextStyle.Default,
            modifier = Modifier
                .background(colorScheme.surface)
                .padding(vertical = 14.dp)
        ) { hour, minute -> viewModel.endTimeStream.value = Time(hour, minute) }

        ErrorTextView(errorText.timeError)

        HorizontalDivider()

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

    SettingsColumnWithChild(
        footer = footerText,
        error = errorText.minSOCError
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(colorScheme.surface)
                .padding(vertical = 4.dp)
        ) {
            Text(
                stringResource(R.string.min_soc),
                Modifier.weight(1.0f),
                color = colorScheme.onSecondary
            )
            OutlinedTextField(
                value = minSOC,
                onValueChange = { viewModel.minSOCStream.value = it.filter { it.isDigit() } },
                modifier = Modifier.width(100.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = colorScheme.onSecondary),
                trailingIcon = { Text("%", color = colorScheme.onSecondary) },
                singleLine = true
            )
        }
    }
}

@Composable
fun MaxSOCView(viewModel: EditPhaseViewModel) {
    val maxSOC = viewModel.maxSocStream.collectAsState().value
    val errorText = viewModel.errorStream.collectAsState().value

    SettingsColumnWithChild(
        error = errorText.maxSOCError
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(colorScheme.surface)
                .padding(vertical = 4.dp)
        ) {
            Text(
                stringResource(R.string.max_soc),
                Modifier.weight(1.0f),
                color = colorScheme.onSecondary
            )
            OutlinedTextField(
                value = maxSOC,
                onValueChange = { viewModel.maxSocStream.value = it.filter { it.isDigit() } },
                modifier = Modifier.width(100.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = colorScheme.onSecondary),
                trailingIcon = { Text("%", color = colorScheme.onSecondary) },
                singleLine = true
            )
        }
    }
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

    SettingsColumnWithChild(
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(colorScheme.surface)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    stringResource(R.string.force_discharge_soc),
                    Modifier.weight(1.0f),
                    color = colorScheme.onSecondary
                )
                OutlinedTextField(
                    value = fdSOC,
                    onValueChange = { viewModel.forceDischargeSOCStream.value = it.filter { it.isDigit() } },
                    modifier = Modifier.width(100.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = colorScheme.onSecondary),
                    trailingIcon = { Text("%", color = colorScheme.onSecondary) },
                    singleLine = true
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
    val errorText = viewModel.errorStream.collectAsState().value

    SettingsColumnWithChild(
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(colorScheme.surface)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    stringResource(R.string.force_discharge_power),
                    Modifier.weight(1.0f),
                    color = colorScheme.onSecondary
                )
                OutlinedTextField(
                    value = fdPower,
                    onValueChange = { viewModel.forceDischargePowerStream.value = it.filter { it.isDigit() } },
                    modifier = Modifier.width(100.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = colorScheme.onSecondary),
                    trailingIcon = { Text("W", color = colorScheme.onSecondary) },
                    singleLine = true
                )
            }
        },
        footer = footerText,
        error = errorText.forceDischargePowerError
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
        Text(
            stringResource(R.string.work_mode),
            color = colorScheme.onSecondary
        )

        Box(contentAlignment = Alignment.TopEnd) {
            ESButton(onClick = { expanded = !expanded }) {
                Text(
                    workMode.title(),
                    color = colorScheme.onPrimary
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = colorScheme.onPrimary
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                viewModel.modes.forEach {
                    DropdownMenuItem(onClick = {
                        expanded = false
                        viewModel.workModeStream.value = it
                    }, text = {
                        Text(it.title())
                    })
                }
            }
        }
    }
}

@Preview(heightDp = 600, widthDp = 400)
@Composable
fun EditPhaseViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Dark) {
        EditPhaseView(
            NavHostController(LocalContext.current),
            FakeUserManager(),
            modifier = Modifier,
            configManager = FakeConfigManager()
        )
    }
}

