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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.W
import com.alpriest.energystats.models.asPercent
import com.alpriest.energystats.models.kW
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SettingsTitleView(title: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1,
            color = colors.primary
        )
    }
}

@Composable
fun BatterySettingsView(config: ConfigManaging, modifier: Modifier = Modifier) {
    val isEditingCapacity = rememberSaveable { mutableStateOf(false) }
    var editingCapacity by rememberSaveable { mutableStateOf(config.batteryCapacity.toString()) }
    val decimalPlaces = config.themeStream.collectAsState().value.decimalPlaces

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        SettingsTitleView("Battery")

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
                text = "Read from your inverter when you login.",
                color = Color.DarkGray
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
                    "Capacity",
                    style = MaterialTheme.typography.h4
                )
                Spacer(Modifier.weight(1f))

                if (isEditingCapacity.value) {
                    OutlinedTextField(
                        value = editingCapacity,
                        onValueChange = {
                            editingCapacity = it
                        },
                        label = { Text("Total W of your battery") },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = config.batteryCapacity.W(decimalPlaces),
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
                        Text("OK")
                    }
                    Button(onClick = { isEditingCapacity.value = false }) {
                        Text("Cancel")
                    }
                }
            }

            Text(
                buildAnnotatedString {
                    append("Calculated as ")
                    withStyle(
                        style = SpanStyle(fontStyle = FontStyle.Italic, color = Color.DarkGray)
                    ) {
                        append("residual / (Min SOC / 100)")
                    }
                    append(" where residual is estimated by your installation and may not be accurate. Tap the capacity above to enter a manual value.")
                },
                color = Color.DarkGray
            )

            Text(
                modifier = Modifier
                    .padding(top = 12.dp),
                text = "Empty/full battery durations are estimates based on calculated capacity, assume that solar conditions and battery charge rates remain constant.",
                color = Color.DarkGray
            )
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