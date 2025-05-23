package com.alpriest.energystats.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SolarStringsSettingsView(config: ConfigManaging) {
    val showSeparateStringsOnPowerFlowState = rememberSaveable { mutableStateOf(config.powerFlowStrings.enabled) }
    val pv1 = rememberSaveable { mutableStateOf(config.powerFlowStrings.pv1Enabled) }
    val pv2 = rememberSaveable { mutableStateOf(config.powerFlowStrings.pv2Enabled) }
    val pv3 = rememberSaveable { mutableStateOf(config.powerFlowStrings.pv3Enabled) }
    val pv4 = rememberSaveable { mutableStateOf(config.powerFlowStrings.pv4Enabled) }
    val pv5 = rememberSaveable { mutableStateOf(config.powerFlowStrings.pv5Enabled) }
    val pv6 = rememberSaveable { mutableStateOf(config.powerFlowStrings.pv6Enabled) }
    val pv1Name = rememberSaveable { mutableStateOf(config.powerFlowStrings.pv1Name) }
    val pv2Name = rememberSaveable { mutableStateOf(config.powerFlowStrings.pv2Name) }
    val pv3Name = rememberSaveable { mutableStateOf(config.powerFlowStrings.pv3Name) }
    val pv4Name = rememberSaveable { mutableStateOf(config.powerFlowStrings.pv4Name) }
    val pv5Name = rememberSaveable { mutableStateOf(config.powerFlowStrings.pv5Name) }
    val pv6Name = rememberSaveable { mutableStateOf(config.powerFlowStrings.pv6Name) }

    SettingsCheckbox(
        title = stringResource(R.string.show_pv_power_by_strings),
        state = showSeparateStringsOnPowerFlowState,
        infoText = stringResource(R.string.solar_strings_description),
        onUpdate = { config.powerFlowStrings = config.powerFlowStrings.copy(enabled = it) }
    )

    AnimatedVisibility(
        visible = showSeparateStringsOnPowerFlowState.value,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            SolarCheckbox(
                stringName = "PV1",
                toggleState = pv1,
                onToggle = {
                    config.powerFlowStrings = config.powerFlowStrings.copy(pv1Enabled = it)
                },
                name = pv1Name,
                onNameChange = {
                    config.powerFlowStrings = config.powerFlowStrings.copy(pv1Name = it)
                }
            )

            SolarCheckbox(
                stringName = "PV2",
                toggleState = pv2,
                onToggle = {
                    config.powerFlowStrings = config.powerFlowStrings.copy(pv2Enabled = it)
                },
                name = pv2Name,
                onNameChange = {
                    config.powerFlowStrings = config.powerFlowStrings.copy(pv2Name = it)
                }
            )

            SolarCheckbox(
                stringName = "PV3",
                toggleState = pv3,
                onToggle = {
                    config.powerFlowStrings = config.powerFlowStrings.copy(pv3Enabled = it)
                },
                name = pv3Name,
                onNameChange = {
                    config.powerFlowStrings = config.powerFlowStrings.copy(pv3Name = it)
                }
            )

            SolarCheckbox(
                stringName = "PV4",
                toggleState = pv4,
                onToggle = {
                    config.powerFlowStrings = config.powerFlowStrings.copy(pv4Enabled = it)
                },
                name = pv4Name,
                onNameChange = {
                    config.powerFlowStrings = config.powerFlowStrings.copy(pv4Name = it)
                }
            )

            SolarCheckbox(
                stringName = "PV5",
                toggleState = pv5,
                onToggle = {
                    config.powerFlowStrings = config.powerFlowStrings.copy(pv5Enabled = it)
                },
                name = pv5Name,
                onNameChange = {
                    config.powerFlowStrings = config.powerFlowStrings.copy(pv5Name = it)
                }
            )

            SolarCheckbox(
                stringName = "PV6",
                toggleState = pv6,
                onToggle = {
                    config.powerFlowStrings = config.powerFlowStrings.copy(pv6Enabled = it)
                },
                name = pv6Name,
                onNameChange = {
                    config.powerFlowStrings = config.powerFlowStrings.copy(pv6Name = it)
                }
            )
        }
    }
}

@Composable
fun SolarCheckbox(stringName: String, toggleState: MutableState<Boolean>, onToggle: (Boolean) -> Unit, name: MutableState<String>, onNameChange: (String) -> Unit) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = name.value,
                onValueChange = {
                    name.value = it
                    onNameChange(it)
                },
                label = { Text(stringName) }
            )

            Checkbox(
                checked = toggleState.value,
                onCheckedChange = {
                    toggleState.value = it
                    onToggle(it)
                },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Preview
@Composable
fun SolarStringsSettingsPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        SettingsColumn {
            SolarStringsSettingsView(config = FakeConfigManager())
        }
    }
}