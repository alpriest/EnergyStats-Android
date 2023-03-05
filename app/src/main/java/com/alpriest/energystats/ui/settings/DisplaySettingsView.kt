package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun DisplaySettingsView(config: ConfigManaging, modifier: Modifier = Modifier) {
    val largeDisplayState = rememberSaveable { mutableStateOf(config.useLargeDisplay) }
    val colouredFlowLinesState = rememberSaveable { mutableStateOf(config.useColouredFlowLines) }
    val showBatteryTemperatureState = rememberSaveable { mutableStateOf(config.showBatteryTemperature) }

    Column(
        modifier = modifier
    ) {
        SettingsTitleView("Display")

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = largeDisplayState.value,
                onCheckedChange = {
                    largeDisplayState.value = it
                    config.useLargeDisplay = it
                },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
            )
            Text("Increase sizes for large display")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = colouredFlowLinesState.value,
                onCheckedChange = {
                    colouredFlowLinesState.value = it
                    config.useColouredFlowLines = it
                },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
            )
            Text("Use coloured flow lines")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = showBatteryTemperatureState.value,
                onCheckedChange = {
                    showBatteryTemperatureState.value = it
                    config.showBatteryTemperature = it
                },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
            )
            Text("Show battery temperature")
        }
    }
}

@Preview(
    showBackground = true,
    heightDp = 640
)
@Composable
fun DisplaySettingsViewPreview() {
    EnergyStatsTheme {
        DisplaySettingsView(config = FakeConfigManager(), modifier = Modifier.padding(horizontal = 12.dp))
    }
}