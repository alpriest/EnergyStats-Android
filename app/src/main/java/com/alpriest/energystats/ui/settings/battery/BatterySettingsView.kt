package com.alpriest.energystats.ui.settings.battery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
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
import com.alpriest.energystats.ui.settings.SettingsNavButton
import com.alpriest.energystats.ui.settings.SettingsButtonList
import com.alpriest.energystats.ui.settings.SettingsCheckbox
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.SettingsScreen
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun BatterySettingsView(config: ConfigManaging, modifier: Modifier = Modifier, navController: NavHostController) {
    val isEditingCapacity = rememberSaveable { mutableStateOf(false) }
    var editingCapacity by rememberSaveable { mutableStateOf(config.batteryCapacity.toString()) }
    val decimalPlaces = config.themeStream.collectAsState().value.decimalPlaces
    val showBatteryEstimateState = rememberSaveable { mutableStateOf(config.showBatteryEstimate) }
    val showUsableBatteryOnlyState = rememberSaveable { mutableStateOf(config.showUsableBatteryOnly) }
    val showBatteryTemperatureState = rememberSaveable { mutableStateOf(config.showBatteryTemperature) }
    val minSOC = config.minSOC.collectAsState()

    SettingsPage {
        SettingsButtonList {
            SettingsNavButton(stringResource(R.string.minimum_charge_levels)) {
                navController.navigate(SettingsScreen.BatterySOC.name)
            }

            SettingsNavButton(stringResource(R.string.charge_times)) {
                navController.navigate(SettingsScreen.BatteryChargeTimes.name)
            }
        }

        SettingsColumnWithChild {
            minSOC.value?.let { minSOC ->
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
                                style = MaterialTheme.typography.h4,
                                color = colors.onSecondary,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = minSOC.asPercent(),
                                color = colors.onSecondary,
                            )
                        }
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
                        style = MaterialTheme.typography.h4,
                        color = colors.onSecondary,
                    )
                    Spacer(Modifier.weight(1f))

                    if (isEditingCapacity.value) {
                        OutlinedTextField(
                            value = editingCapacity,
                            onValueChange = {
                                editingCapacity = it
                            },
                            label = {
                                Text(
                                    stringResource(R.string.total_wh_of_your_battery),
                                    color = colors.onSecondary
                                )
                            },
                            textStyle = TextStyle(colors.onSecondary),
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = config.batteryCapacity.Wh(decimalPlaces),
                            modifier = Modifier.clickable { isEditingCapacity.value = true },
                            color = colors.onSecondary,
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
                            Text(
                                stringResource(R.string.ok),
                                color = colors.onPrimary,
                            )
                        }
                        Button(onClick = { isEditingCapacity.value = false }) {
                            Text(
                                stringResource(R.string.cancel),
                                color = colors.onPrimary,
                            )
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
            }
        }

        SettingsColumnWithChild {
            SettingsCheckbox(
                title = stringResource(R.string.show_battery_full_empty_estimate),
                state = showBatteryEstimateState,
                onConfigUpdate = { config.showBatteryEstimate = it },
                footer = buildAnnotatedString { append(stringResource(R.string.empty_full_battery_durations_are_estimates_based_on_calculated_capacity_assume_that_solar_conditions_and_battery_charge_rates_remain_constant)) }
            )

            SettingsCheckbox(
                title = stringResource(R.string.show_usable_battery_only),
                state = showUsableBatteryOnlyState,
                onConfigUpdate = { config.showUsableBatteryOnly = it },
                footer = buildAnnotatedString { append(stringResource(R.string.deducts_the_min_soc_amount_from_the_battery_charge_level_and_percentage_due_to_inaccuracies_in_the_way_battery_levels_are_measured_this_may_result_in_occasionally_showing_a_negative_amount_remaining)) }
            )

            SettingsCheckbox(
                title = stringResource(R.string.show_battery_temperature),
                state = showBatteryTemperatureState,
                onConfigUpdate = { config.showBatteryTemperature = it }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BatterySettingsViewPreview() {
    EnergyStatsTheme(darkTheme = false) {
        BatterySettingsView(
            config = FakeConfigManager(),
            navController = NavHostController(LocalContext.current)
        )
    }
}