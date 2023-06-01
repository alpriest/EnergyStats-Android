package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Wh
import com.alpriest.energystats.models.asPercent
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
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
fun BatterySettingsView(config: ConfigManaging, modifier: Modifier = Modifier) {
    val isEditingCapacity = rememberSaveable { mutableStateOf(false) }
    var editingCapacity by rememberSaveable { mutableStateOf(config.batteryCapacity.toString()) }
    val decimalPlaces = config.themeStream.collectAsState().value.decimalPlaces

    RoundedColumnWithChild {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
        ) {
            SettingsTitleView(stringResource(R.string.battery))

            Column(modifier = modifier) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "Min SOC",
                        style = MaterialTheme.typography.h4
                    )
                    Spacer(Modifier.weight(1f))
                    Text(text = config.minSOC.asPercent())
                }

                Text(
                    text = stringResource(R.string.read_from_your_inverter_when_you_login),
                    color = colors.onSecondary
                )
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
    }
}

@Preview(showBackground = true)
@Composable
fun BatterySettingsViewPreview() {
    EnergyStatsTheme {
        BatterySettingsView(
            config = FakeConfigManager()
        )
    }
}