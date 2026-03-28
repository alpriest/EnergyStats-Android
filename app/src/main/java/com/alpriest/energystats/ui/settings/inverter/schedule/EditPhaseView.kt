package com.alpriest.energystats.ui.settings.inverter.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.models.ColorThemeMode
import com.alpriest.energystats.shared.models.TimeType
import com.alpriest.energystats.shared.models.WorkModes
import com.alpriest.energystats.shared.ui.PaleWhite
import com.alpriest.energystats.shared.ui.PowerFlowNegative
import com.alpriest.energystats.ui.dialog.MonitorAlertDialog
import com.alpriest.energystats.ui.settings.BottomButtonConfiguration
import com.alpriest.energystats.ui.settings.ContentWithBottomButtons
import com.alpriest.energystats.ui.settings.ErrorTextView
import com.alpriest.energystats.ui.settings.InfoButton
import com.alpriest.energystats.ui.settings.SettingsBottomSpace
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPaddingValues
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.SettingsTitleView
import com.alpriest.energystats.ui.settings.battery.TimePeriodView
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

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
    configManager: ConfigManaging,
    viewModel: EditPhaseViewModel = viewModel(factory = EditPhaseViewModelFactory(navController, configManager)),
    modifier: Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(null) {
        viewModel.load(context)
    }
    trackScreenView("Edit phase", "EditPhaseView")

    ContentWithBottomButtons(
        buttons = listOf(
            BottomButtonConfiguration(title = stringResource(R.string.cancel), onTap = { navController.popBackStack() }),
            BottomButtonConfiguration(title = stringResource(R.string.apply), onTap = { viewModel.save(context) }),
        ),
        content = { innerModifier ->
            SettingsPage(innerModifier) {
                TimeAndWorkModeView(viewModel)

                StandardViews(viewModel)
                AdvancedViews(viewModel)

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
        modifier = modifier
    )
}

@Composable
fun TimeAndWorkModeView(viewModel: EditPhaseViewModel) {
    val viewData = viewModel.viewDataStream.collectAsState().value
    val errorText = viewModel.timeErrorStream.collectAsState().value
    val timeTypeShowing = remember { mutableStateOf<TimeType?>(null) }

    MonitorAlertDialog(viewModel)

    SettingsColumnWithChild {
        TimePeriodView(
            viewData.startTime,
            TimeType.START,
            stringResource(R.string.start_time),
            labelStyle = TextStyle.Default,
            includeSeconds = false,
            modifier = Modifier
                .background(colorScheme.surface)
                .padding(vertical = 14.dp),
            timeTypeShowing = timeTypeShowing
        ) { time -> viewModel.startTimeChanged(time) }

        HorizontalDivider()

        TimePeriodView(
            viewData.endTime,
            TimeType.END,
            stringResource(R.string.end_time),
            labelStyle = TextStyle.Default,
            includeSeconds = false,
            modifier = Modifier
                .background(colorScheme.surface)
                .padding(vertical = 14.dp),
            timeTypeShowing = timeTypeShowing
        ) { time -> viewModel.endTimeChanged(time) }

        ErrorTextView(errorText, modifier = Modifier.padding(8.dp))

        HorizontalDivider()

        WorkModeView(viewModel)
    }
}

@Composable
fun StandardViews(viewModel: EditPhaseViewModel) {
    val fieldErrors = viewModel.errorStream.collectAsState().value
    val viewData = viewModel.viewDataStream.collectAsState().value

    viewModel.viewDataStream.collectAsState().value.fields.filter { it.isStandard }.forEach { phaseFieldDefinition ->
        EditableItemView(
             if (phaseFieldDefinition.value == null) "" else phaseFieldDefinition.value.toInt().toString(),
            fieldErrors[phaseFieldDefinition.key],
            null,
            title(phaseFieldDefinition.key, viewData.workMode),
            phaseFieldDefinition.unit
        ) {
            viewModel.phaseFieldChanged(phaseFieldDefinition, it)
        }
    }
}

@Composable
fun AdvancedViews(viewModel: EditPhaseViewModel) {
    var showingAdvanced by remember { mutableStateOf(false) }
    val viewData = viewModel.viewDataStream.collectAsState().value
    val fieldErrors = viewModel.errorStream.collectAsState().value

    if (viewData.showAdvancedFields) {
        SettingsColumnWithChild(
            modifier = Modifier.clickable { showingAdvanced = !showingAdvanced }
        ) {
            SettingsTitleView(
                "Advanced",
                modifier = Modifier.clickable { showingAdvanced = !showingAdvanced },
                extra = {
                    Icon(
                        imageVector = if (showingAdvanced) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                    )
                }
            )
        }

        if (showingAdvanced) {
            viewModel.viewDataStream.collectAsState().value.fields.filter { !it.isStandard }.forEach { phaseFieldDefinition ->
                EditableItemView(
                    if (phaseFieldDefinition.value == null) "" else phaseFieldDefinition.value.toInt().toString(),
                    fieldErrors[phaseFieldDefinition.key],
                    null,
                    title(phaseFieldDefinition.key, viewData.workMode),
                    phaseFieldDefinition.unit
                ) {
                    viewModel.phaseFieldChanged(phaseFieldDefinition, it)
                }
            }
        }

        if (showingAdvanced) {
            Text(
                "These settings are optional and can usually be left as default.",
                Modifier.padding(SettingsPaddingValues.default())
            )
        }
    }
}

@Composable
private fun title(key: String, workMode: String): String {
    return when (key) {
        "minsocongrid" ->  stringResource(R.string.min_soc)
        "fdsoc" if workMode == WorkModes.ForceDischarge -> stringResource(R.string.force_discharge_soc)
        "fdpwr" if workMode == WorkModes.ForceDischarge -> stringResource(R.string.force_discharge_power)
        "fdsoc" if workMode == WorkModes.ForceCharge -> stringResource(R.string.force_charge_soc)
        "fdpwr" if workMode == WorkModes.ForceCharge -> stringResource(R.string.force_charge_power)
        else -> key
    }
}

//@Composable
//fun MinSOCView(viewModel: EditPhaseViewModel) {
//    val workMode = viewModel.workModeStream.collectAsState().value
//    val minSOC = viewModel.minSOCStream.collectAsState().value
//    val footerText = when (workMode) {
//        WorkModes.ForceDischarge -> stringResource(R.string.force_discharge_timeperiod_minsoc_description)
//        else -> null
//    }
//    val errorText = viewModel.errorStream.collectAsState().value
//
//    EditableItemView(
//        minSOC,
//        errorText.minSOCError,
//        footerText,
//        stringResource(R.string.min_soc),
//        "%",
//        { viewModel.minSOCStream.value = it.filter { it.isDigit() } }
//    )
//}
//
//@Composable
//fun MaxSOCView(viewModel: EditPhaseViewModel) {
//    val maxSOC = viewModel.maxSocStream.collectAsState().value
//    val errorText = viewModel.errorStream.collectAsState().value
//
//    EditableItemView(
//        maxSOC,
//        errorText.maxSOCError,
//        footerText = null,
//        stringResource(SharedR.string.max_soc),
//        "%",
//        { viewModel.maxSocStream.value = it.filter { it.isDigit() } }
//    )
//}
//
//@Composable
//fun ForceDischargeSOCView(viewModel: EditPhaseViewModel) {
//    val workMode = viewModel.workModeStream.collectAsState().value
//    val fdSOC = viewModel.forceDischargeSOCStream.collectAsState().value
//    val footerText = when (workMode) {
//        WorkModes.ForceDischarge -> stringResource(R.string.force_discharge_timeperiod_fdsoc_description)
//        else -> null
//    }
//    val errorText = viewModel.errorStream.collectAsState().value
//
//    EditableItemView(
//        fdSOC,
//        errorText.fdSOCError,
//        footerText,
//        stringResource(R.string.force_discharge_soc),
//        "%",
//        { viewModel.forceDischargeSOCStream.value = it.filter { it.isDigit() } }
//    )
//}
//
//@Composable
//fun ForceDischargePowerView(viewModel: EditPhaseViewModel) {
//    val workMode = viewModel.workModeStream.collectAsState().value
//    val fdPower = viewModel.forceDischargePowerStream.collectAsState().value
//    val footerText = when (workMode) {
//        WorkModes.ForceDischarge -> stringResource(R.string.force_discharge_timeperiod_power_description)
//        else -> null
//    }
//    val errorText = viewModel.errorStream.collectAsState().value
//
//    EditableItemView(
//        fdPower,
//        errorText.forceDischargePowerError,
//        footerText,
//        stringResource(R.string.force_discharge_power),
//        "W",
//        { viewModel.forceDischargePowerStream.value = it.filter { it.isDigit() } }
//    )
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkModeView(viewModel: EditPhaseViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val viewData = viewModel.viewDataStream.collectAsState().value
    val context = LocalContext.current
    val footerText = when (viewData.workMode) {
        WorkModes.SelfUse -> stringResource(R.string.workmode_self_use_description)
        WorkModes.Feedin -> stringResource(R.string.workmode_feed_in_first_description)
        WorkModes.Backup -> stringResource(R.string.workmode_backup_description)
        WorkModes.ForceCharge -> stringResource(R.string.workmode_force_charge_description)
        WorkModes.ForceDischarge -> stringResource(R.string.workmode_force_discharge_description)
        else -> null
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.work_mode),
                color = colorScheme.onSecondary
            )
            footerText?.let { InfoButton(it) }
        }

        Box(contentAlignment = Alignment.TopEnd) {
            ESButton(onClick = { expanded = !expanded }) {
                Text(
                    viewData.workMode.title(context),
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
                        viewModel.workModeChanged(it)
                    }, text = {
                        Text(it.title(context))
                    })
                }
            }
        }
    }
}

@Composable
private fun EditableItemView(
    value: String,
    errorText: String?,
    footerText: String?,
    title: String,
    unit: String?,
    onValueChange: (String) -> Unit
) {
    SettingsColumnWithChild(
        error = errorText,
        footer = footerText
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(colorScheme.surface)
                .padding(vertical = 4.dp)
        ) {
            Text(
                title,
                Modifier.weight(1.0f),
                color = colorScheme.onSecondary
            )
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.width(120.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, color = colorScheme.onSecondary),
                trailingIcon = { unit?.let { Text(it, color = colorScheme.onSecondary) } },
                singleLine = true
            )
        }
    }
}

@Preview(heightDp = 600, widthDp = 400)
@Composable
fun EditPhaseViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        EditPhaseView(
            NavHostController(LocalContext.current),
            modifier = Modifier,
            configManager = FakeConfigManager()
        )
    }
}

