package com.alpriest.energystats.ui.settings.inverter

import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun InverterChoiceView(
    configManager: ConfigManaging
) {
    var expanded by remember { mutableStateOf(false) }
    val currentDevice = configManager.currentDevice.collectAsState()

    if (configManager.devices.isNotEmpty()) {
        SettingsColumn(header = stringResource(R.string.device_selection)) {
            currentDevice.value?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Inverter",
                        color = colorScheme.onSecondary
                    )

                    Box(contentAlignment = Alignment.TopEnd) {
                        ESButton(
                            onClick = { expanded = !expanded }
                        ) {
                            Text(
                                it.deviceDisplayName,
                                fontSize = 12.sp,
                                color = colorScheme.onPrimary,
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = colorScheme.onPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            configManager.devices.forEach { device ->
                                DropdownMenuItem(onClick = {
                                    expanded = false
                                    configManager.select(device)
                                }, text = {
                                    Text(text = device.deviceDisplayName)
                                })
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
        return listOfNotNull(stationName).firstOrNull() ?: ""
    }

@Preview(showBackground = true, heightDp = 600, widthDp = 400)
@Composable
fun InverterChoiceViewPreview() {
    EnergyStatsTheme {
        InverterChoiceView(FakeConfigManager())
    }
}