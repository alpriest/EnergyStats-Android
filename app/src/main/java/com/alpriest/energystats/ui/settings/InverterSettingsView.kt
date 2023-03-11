package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun DeviceSettingsView(config: ConfigManaging) {
    var expanded by remember { mutableStateOf(false) }

    if ((config.devices?.count() ?: 0) > 1) {
        Column {
            SettingsTitleView("Device selection")

            config.currentDevice?.let {
                Box {
                    Button(onClick = { expanded = !expanded }) {
                        Text(
                            it.plantName,
                            fontSize = 10.sp
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        config.devices?.forEach { device ->
                            DropdownMenuItem(onClick = {
                                expanded = false
                                config.selectedDeviceID = device.deviceID
                            }) {
                                Text(text = device.plantName)
                            }
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(top = 24.dp))
        }
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
fun InverterSettingsViewPreview() {
    EnergyStatsTheme {
        DeviceSettingsView(FakeConfigManager())
    }
}