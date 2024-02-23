package com.alpriest.energystats.ui.settings.inverter

import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsTitleView
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun InverterChoiceView(
    configManager: ConfigManaging
) {
    var expanded by remember { mutableStateOf(false) }
    val currentDevice = configManager.currentDevice.collectAsState()

    if ((configManager.devices?.count() ?: 0) > 1) {
        SettingsColumnWithChild {
            SettingsTitleView("Device selection")

            currentDevice.value?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Inverter",
                        color = colors.onSecondary
                    )

                    Box(contentAlignment = Alignment.TopEnd) {
                        Button(onClick = { expanded = !expanded }) {
                            Text(
                                it.deviceDisplayName,
                                fontSize = 12.sp,
                                color = colors.onPrimary,
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = colors.onPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            configManager.devices?.forEach { device ->
                                DropdownMenuItem(onClick = {
                                    expanded = false
                                    configManager.select(device)
                                }) {
                                    Text(text = device.deviceDisplayName)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

val Device.deviceDisplayName: String
    get() {
        return listOfNotNull(stationName, stationID).joinToString { it }
    }

@Preview(showBackground = true, heightDp = 600, widthDp = 400)
@Composable
fun InverterChoiceViewPreview() {
    EnergyStatsTheme {
        InverterChoiceView(FakeConfigManager())
    }
}