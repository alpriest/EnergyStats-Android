package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val showSunnyBackgroundState = rememberSaveable { mutableStateOf(config.showSunnyBackground) }
    val decimalPlacesState = rememberSaveable { mutableStateOf(config.decimalPlaces) }
    val showBatteryEstimateState = rememberSaveable { mutableStateOf(config.showBatteryEstimate) }
    val showUsableBatteryOnlyState = rememberSaveable { mutableStateOf(config.showUsableBatteryOnly) }
    val showTotalYieldState = rememberSaveable { mutableStateOf(config.showTotalYield) }

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
            Text("Show coloured flow lines")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = showTotalYieldState.value,
                onCheckedChange = {
                    showTotalYieldState.value = it
                    config.showTotalYield = it
                },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
            )
            Text("Show total yield")
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

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = showBatteryEstimateState.value,
                onCheckedChange = {
                    showBatteryEstimateState.value = it
                    config.showBatteryEstimate = it
                },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
            )
            Text("Show battery full/empty estimate")
        }

        Row(
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = showUsableBatteryOnlyState.value,
                onCheckedChange = {
                    showUsableBatteryOnlyState.value = it
                    config.showUsableBatteryOnly = it
                },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
            )

            Column {
                Text(
                    modifier = Modifier.padding(top = 14.dp),
                    text = "Show usable battery only"
                )

                Text(
                    modifier = Modifier,
                    text = "Deducts the Min SOC amount from the battery charge level and percentage. Due to inaccuracies in the way battery levels are measured this may result in occasionally showing a negative amount remaining.",
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.caption
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = showSunnyBackgroundState.value,
                onCheckedChange = {
                    showSunnyBackgroundState.value = it
                    config.showSunnyBackground = it
                },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
            )
            Text("Show sunny background")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Decimal places")
            listOf(2, 3).map {
                RadioButton(
                    selected = decimalPlacesState.value == it,
                    onClick = {
                        decimalPlacesState.value = it
                        config.decimalPlaces = it
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary)
                )
                Text(
                    it.toString(),
                    color = Color.DarkGray
                )
            }
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