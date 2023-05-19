package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun InverterChoiceView(config: ConfigManaging) {
    var expanded by remember { mutableStateOf(false) }
    val currentDevice = config.currentDevice.collectAsState()

    if ((config.devices?.count() ?: 0) > 1) {
        RoundedColumnWithChild {
            SettingsTitleView("Device selection")

            currentDevice.value?.let {
                Row {
                    Button(onClick = { expanded = !expanded }) {
                        Text(
                            it.deviceDisplayName,
                            fontSize = 12.sp
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
                                config.select(device)
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

private val Device.deviceDisplayName: String
    get() {
        return deviceType ?: "$deviceID Re-login to update"
    }

@Preview(showBackground = true, heightDp = 600)
@Composable
fun InverterSettingsViewPreview() {
    EnergyStatsTheme {
        InverterChoiceView(FakeConfigManager())
    }
}