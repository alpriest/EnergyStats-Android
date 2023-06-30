package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

enum class SelfSufficiencyEstimateMode(val value: Int) {
    Off(0),
    Net(1),
    Absolute(2);

    fun title(): String {
        return when (this) {
            Net -> "Net"
            Absolute -> "Absolute"
            else -> "Off"
        }
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}

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

    RoundedColumnWithChild(
        modifier = modifier
    ) {
        SettingsTitleView(stringResource(R.string.display))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    largeDisplayState.value = !largeDisplayState.value
                    config.useLargeDisplay = largeDisplayState.value
                }
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = largeDisplayState.value,
                onCheckedChange = {
                    largeDisplayState.value = it
                    config.useLargeDisplay = it
                },
                colors = CheckboxDefaults.colors(checkedColor = colors.primary)
            )
            Text(stringResource(R.string.increase_sizes_for_large_display))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    colouredFlowLinesState.value = !colouredFlowLinesState.value
                    config.useColouredFlowLines = colouredFlowLinesState.value
                }
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = colouredFlowLinesState.value,
                onCheckedChange = {
                    colouredFlowLinesState.value = it
                    config.useColouredFlowLines = it
                },
                colors = CheckboxDefaults.colors(checkedColor = colors.primary)
            )
            Text(stringResource(R.string.show_coloured_flow_lines))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    showTotalYieldState.value = !showTotalYieldState.value
                    config.showTotalYield = showTotalYieldState.value
                }
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = showTotalYieldState.value,
                onCheckedChange = {
                    showTotalYieldState.value = it
                    config.showTotalYield = it
                },
                colors = CheckboxDefaults.colors(checkedColor = colors.primary)
            )
            Text(stringResource(R.string.show_total_yield))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    showBatteryTemperatureState.value = !showBatteryTemperatureState.value
                    config.showBatteryTemperature = showBatteryTemperatureState.value
                }
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = showBatteryTemperatureState.value,
                onCheckedChange = {
                    showBatteryTemperatureState.value = it
                    config.showBatteryTemperature = it
                },
                colors = CheckboxDefaults.colors(checkedColor = colors.primary)
            )
            Text(stringResource(R.string.show_battery_temperature))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    showBatteryEstimateState.value = !showBatteryEstimateState.value
                    config.showBatteryEstimate = showBatteryEstimateState.value
                }
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = showBatteryEstimateState.value,
                onCheckedChange = {
                    showBatteryEstimateState.value = it
                    config.showBatteryEstimate = it
                },
                colors = CheckboxDefaults.colors(checkedColor = colors.primary)
            )
            Text(stringResource(R.string.show_battery_full_empty_estimate))
        }

        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .clickable {
                    showUsableBatteryOnlyState.value = !showUsableBatteryOnlyState.value
                    config.showUsableBatteryOnly = showUsableBatteryOnlyState.value
                }
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = showUsableBatteryOnlyState.value,
                onCheckedChange = {
                    showUsableBatteryOnlyState.value = it
                    config.showUsableBatteryOnly = it
                },
                colors = CheckboxDefaults.colors(checkedColor = colors.primary)
            )

            Column {
                Text(
                    modifier = Modifier.padding(top = 14.dp),
                    text = stringResource(R.string.show_usable_battery_only)
                )

                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.deducts_the_min_soc_amount_from_the_battery_charge_level_and_percentage_due_to_inaccuracies_in_the_way_battery_levels_are_measured_this_may_result_in_occasionally_showing_a_negative_amount_remaining),
                    color = colors.onSecondary,
                    style = MaterialTheme.typography.caption
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    showSunnyBackgroundState.value = !showSunnyBackgroundState.value
                    config.showSunnyBackground = showSunnyBackgroundState.value
                }
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = showSunnyBackgroundState.value,
                onCheckedChange = {
                    showSunnyBackgroundState.value = it
                    config.showSunnyBackground = it
                },
                colors = CheckboxDefaults.colors(checkedColor = colors.primary)
            )
            Text(stringResource(R.string.show_sunny_background))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.decimal_places))
            listOf(2, 3).map {
                RadioButton(
                    selected = decimalPlacesState.value == it,
                    onClick = {
                        decimalPlacesState.value = it
                        config.decimalPlaces = it
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
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