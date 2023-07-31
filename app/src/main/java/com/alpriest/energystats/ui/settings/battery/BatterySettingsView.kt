package com.alpriest.energystats.ui.settings.battery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Wh
import com.alpriest.energystats.models.asPercent
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.SettingsButton
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsScreen
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SettingsTitleView(title: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.h4
        )
    }
}

@Composable
fun BatterySettingsView(config: ConfigManaging, modifier: Modifier = Modifier, navController: NavHostController) {
    val isEditingCapacity = rememberSaveable { mutableStateOf(false) }
    var editingCapacity by rememberSaveable { mutableStateOf(config.batteryCapacity.toString()) }
    val decimalPlaces = config.themeStream.collectAsState().value.decimalPlaces
    val showBatteryEstimateState = rememberSaveable { mutableStateOf(config.showBatteryEstimate) }
    val showUsableBatteryOnlyState = rememberSaveable { mutableStateOf(config.showUsableBatteryOnly) }
    val showBatteryTemperatureState = rememberSaveable { mutableStateOf(config.showBatteryTemperature) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SettingsButton(stringResource(R.string.minimum_charge_levels)) {
            navController.navigate(SettingsScreen.BatterySOC.name)
        }

        SettingsButton(stringResource(R.string.charge_times)) {
            navController.navigate(SettingsScreen.BatteryChargeTimes.name)
        }

        SettingsColumnWithChild {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = modifier
            ) {
                Column(modifier = modifier) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            stringResource(R.string.min_battery_charge_soc),
                            style = MaterialTheme.typography.h4
                        )
                        Spacer(Modifier.weight(1f))
                        Text(text = config.minSOC.asPercent())
                    }
                }
            }

            Column(modifier = modifier) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        stringResource(R.string.capacity),
                        style = MaterialTheme.typography.h4
                    )
                    Spacer(Modifier.weight(1f))

                    if (isEditingCapacity.value) {
                        OutlinedTextField(
                            value = editingCapacity,
                            onValueChange = {
                                editingCapacity = it
                            },
                            label = { Text(stringResource(R.string.total_wh_of_your_battery)) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = config.batteryCapacity.Wh(decimalPlaces),
                            modifier = Modifier.clickable { isEditingCapacity.value = true }
                        )
                    }
                }

                if (isEditingCapacity.value) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Button(
                            onClick = {
                                config.updateBatteryCapacity(editingCapacity)
                                isEditingCapacity.value = false
                            }
                        ) {
                            Text(stringResource(R.string.ok))
                        }
                        Button(onClick = { isEditingCapacity.value = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }

                Text(
                    buildAnnotatedString {
                        append(stringResource(R.string.calculated_as))
                        withStyle(
                            style = SpanStyle(fontStyle = FontStyle.Italic, color = colors.onSecondary)
                        ) {
                            append("residual / (Min SOC / 100)")
                        }
                        append(stringResource(R.string.where_residual_is_estimated_by_your_installation_and_may_not_be_accurate_tap_the_capacity_above_to_enter_a_manual_value))
                    },
                    color = colors.onSecondary
                )

                Text(
                    modifier = Modifier
                        .padding(top = 12.dp),
                    text = stringResource(R.string.empty_full_battery_durations_are_estimates_based_on_calculated_capacity_assume_that_solar_conditions_and_battery_charge_rates_remain_constant),
                    color = colors.onSecondary
                )
            }
        }

        SettingsColumnWithChild {
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BatterySettingsViewPreview() {
    EnergyStatsTheme {
        BatterySettingsView(
            config = FakeConfigManager(),
            navController = NavHostController(LocalContext.current)
        )
    }
}