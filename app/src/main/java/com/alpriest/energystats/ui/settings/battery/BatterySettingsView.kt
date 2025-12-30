package com.alpriest.energystats.ui.settings.battery

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.alpriest.energystats.shared.helpers.Wh
import com.alpriest.energystats.shared.models.network.DeviceSettingsItem
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.helpers.SegmentedControl
import com.alpriest.energystats.ui.settings.BatteryTemperatureDisplayMode
import com.alpriest.energystats.ui.settings.InlineSettingsNavButton
import com.alpriest.energystats.ui.settings.SettingsCheckbox
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsPage
import com.alpriest.energystats.ui.settings.SettingsScreen
import com.alpriest.energystats.ui.settings.SettingsSegmentedControl
import com.alpriest.energystats.ui.settings.SettingsTitleView
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun BatterySettingsView(config: ConfigManaging, modifier: Modifier = Modifier, navController: NavHostController) {
    val isEditingCapacity = rememberSaveable { mutableStateOf(false) }
    var editingCapacity by rememberSaveable { mutableStateOf(config.batteryCapacity.toString()) }
    val decimalPlaces = config.themeStream.collectAsState().value.decimalPlaces
    val showBatteryEstimateState = rememberSaveable { mutableStateOf(config.showBatteryEstimate) }
    val showUsableBatteryOnlyState = rememberSaveable { mutableStateOf(config.showUsableBatteryOnly) }
    val showBatteryTemperatureState = rememberSaveable { mutableStateOf(config.showBatteryTemperature) }
    val showBatterySOCOnDailyStatsState = rememberSaveable { mutableStateOf(config.showBatterySOCOnDailyStats) }
    val hasError = config.currentDevice.collectAsState().value?.battery?.hasError ?: false
    val batteryTemperatureDisplayModeState = rememberSaveable { mutableStateOf(config.batteryTemperatureDisplayMode) }
    val context = LocalContext.current

    trackScreenView("Battery", "BatterySettingsView")

    SettingsPage(modifier) {
        if (hasError) {
            SettingsColumnWithChild(modifier = Modifier.border(width = 2.dp, color = Color.Red)) {
                SettingsTitleView(stringResource(R.string.error))
                Text(stringResource(R.string.battery_errors_description_1))
            }
        }

        SettingsColumn {
            InlineSettingsNavButton(stringResource(R.string.minimum_charge_levels)) {
                navController.navigate(SettingsScreen.BatterySOC.name)
            }

            HorizontalDivider()

            InlineSettingsNavButton(stringResource(R.string.charge_times)) {
                navController.navigate(SettingsScreen.BatteryChargeTimes.name)
            }

            HorizontalDivider()

            InlineSettingsNavButton(DeviceSettingsItem.MaxSoc.title(context)) {
                navController.navigate(SettingsScreen.ConfigureMaxSoc.name)
            }
        }

        SettingsColumn(
            header = "Display options",
            footerAnnotatedString = buildAnnotatedString {
                append(stringResource(R.string.battery_capacity_formula_explanation_prefix))
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append("residual / (Min SOC / 100)")
                }
                append(stringResource(R.string.battery_capacity_formula_explanation_suffix))
            },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    stringResource(R.string.storage_capacity),
                    color = colorScheme.onSecondary,
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
                                color = colorScheme.onSecondary
                            )
                        },
                        textStyle = TextStyle(colorScheme.onSecondary),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = config.batteryCapacity.Wh(decimalPlaces),
                        modifier = Modifier.clickable { isEditingCapacity.value = true },
                        color = colorScheme.onSecondary,
                    )
                }
            }

            if (isEditingCapacity.value) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        ESButton(
                            onClick = {
                                try {
                                    config.batteryCapacity = editingCapacity.toInt()
                                } catch (_: NumberFormatException) {
                                }
                                isEditingCapacity.value = false
                            }
                        ) {
                            Text(
                                stringResource(R.string.ok),
                                color = colorScheme.onPrimary,
                            )
                        }
                        ESButton(onClick = { isEditingCapacity.value = false }) {
                            Text(
                                stringResource(R.string.cancel),
                                color = colorScheme.onPrimary,
                            )
                        }
                    }
                }
            }
        }
        SettingsColumn(
            footer = stringResource(R.string.empty_full_battery_durations_are_estimates_based_on_calculated_capacity_assume_that_solar_conditions_and_battery_charge_rates_remain_constant)
        ) {
            SettingsCheckbox(
                title = stringResource(R.string.show_battery_full_empty_estimate),
                state = showBatteryEstimateState,
                onUpdate = { config.showBatteryEstimate = it },
            )
        }

        SettingsColumn(
            footer = stringResource(R.string.deducts_the_min_soc_amount_from_the_battery_charge_level_and_percentage_due_to_inaccuracies_in_the_way_battery_levels_are_measured_this_may_result_in_occasionally_showing_a_negative_amount_remaining)
        ) {
            SettingsCheckbox(
                title = stringResource(R.string.show_usable_battery_only),
                state = showUsableBatteryOnlyState,
                onUpdate = { config.showUsableBatteryOnly = it }
            )
        }

        SettingsColumn(
            footer = stringResource(R.string.battery_temperature_footer)
        ) {
            SettingsCheckbox(
                title = stringResource(R.string.show_battery_temperature),
                state = showBatteryTemperatureState,
                onUpdate = { config.showBatteryTemperature = it }
            )
        }

        SettingsColumn(
            footer = batteryTemperateDisplayModeFooter(batteryTemperatureDisplayModeState.value)
        ) {
            SettingsSegmentedControl(
                title = stringResource(R.string.display_battery_stack),
                segmentedControl = {
                    val items = listOf(BatteryTemperatureDisplayMode.Automatic, BatteryTemperatureDisplayMode.Battery1, BatteryTemperatureDisplayMode.Battery2)
                    SegmentedControl(
                        items = items.map { it.title(context) },
                        defaultSelectedItemIndex = items.indexOf(batteryTemperatureDisplayModeState.value),
                        color = colorScheme.primary
                    ) {
                        batteryTemperatureDisplayModeState.value = items[it]
                        config.batteryTemperatureDisplayMode = items[it]
                    }
                }
            )
        }

        SettingsColumn(
            footer = stringResource(R.string.show_battery_soc_on_daily_stats_description)
        ) {
            SettingsCheckbox(
                title = stringResource(R.string.show_battery_soc_on_daily_stats),
                state = showBatterySOCOnDailyStatsState,
                onUpdate = { config.showBatterySOCOnDailyStats = it }
            )
        }

        SettingsColumn {
            InlineSettingsNavButton(stringResource(R.string.battery_versions)) {
                navController.navigate(SettingsScreen.BatteryVersions.name)
            }
        }
    }
}

@Composable
private fun batteryTemperateDisplayModeFooter(value: BatteryTemperatureDisplayMode): String {
    val context = LocalContext.current

    return when (value) {
        BatteryTemperatureDisplayMode.Automatic -> context.getString(R.string.batteryTemperatureDisplayMode_automatic)
        BatteryTemperatureDisplayMode.Battery1 -> String.format(stringResource(R.string.batteryTemperatureDisplayMode_batteryN), "1")
        BatteryTemperatureDisplayMode.Battery2 -> String.format(stringResource(R.string.batteryTemperatureDisplayMode_batteryN), "2")
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