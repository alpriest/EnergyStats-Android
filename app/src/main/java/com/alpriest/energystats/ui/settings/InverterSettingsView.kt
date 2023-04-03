package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceAround
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.models.DeviceFirmwareVersion
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.launch

@Composable
fun DeviceSettingsView(config: ConfigManaging) {
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var firmwareVersion by remember { mutableStateOf<DeviceFirmwareVersion?>(null) }
    val uriHandler = LocalUriHandler.current

    Column {

        if ((config.devices?.count() ?: 0) > 1) {
            Column {
                SettingsTitleView("Device selection")

                config.currentDevice?.let {
                    Row {
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
            }
        }

        Divider(modifier = Modifier.padding(top = 24.dp))

        firmwareVersion?.let {
            SettingsTitleView("Firmware versions")
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Column { Text("Manager: " + it.manager) }
                Column { Text("Slave: " + it.slave) }
                Column { Text("Master: " + it.master) }
            }

            Text(
                text = "Find out more about firmware versions from the foxesscommunity.com website",
                color = Color.DarkGray,
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://foxesscommunity.com/viewforum.php?f=29")
                }
            )
        }

        Divider(modifier = Modifier.padding(top = 24.dp))
    }.run {
        coroutineScope.launch {
            firmwareVersion = config.fetchFirmwareVersions()
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