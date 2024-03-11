package com.alpriest.energystats.ui.settings.inverter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.DeviceFirmwareVersion
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.ui.settings.SettingsColumn

@Composable
fun SettingsRow(title: String, value: String?) {
    SettingsRow(title) {
        Text(value ?: "(unknown)", color = colors.onSecondary)
    }
}

@Composable
fun SettingsRow(title: String, content: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .defaultMinSize(minHeight = 30.dp)
    ) {
        Text(
            title,
            color = colors.onSecondary,
            modifier = Modifier.padding(end = 8.dp)
        )
        content()
    }
}

@Composable
fun FirmwareVersionView(device: Device, network: Networking) {
    val uriHandler = LocalUriHandler.current
    var firmware: DeviceFirmwareVersion? by remember { mutableStateOf(null) }

    LaunchedEffect(null) {
        var response = network.fetchDevice(device.deviceSN)
        firmware = DeviceFirmwareVersion(manager = response.managerVersion, slave = response.slaveVersion, master = response.masterVersion)
    }

    firmware?.let {
        SettingsColumn(
            header = stringResource(R.string.firmware_versions),
        ) {
            SettingsRow("Manager", it.manager)
            SettingsRow("Slave", it.slave)
            SettingsRow("Master", it.master)

            Text(
                text = stringResource(R.string.find_out_more_about_firmware_versions_from_the_foxesscommunity_com_website),
                color = colors.onSecondary,
                style = MaterialTheme.typography.caption,
                modifier = Modifier
                    .clickable {
                        uriHandler.openUri("https://foxesscommunity.com/viewforum.php?f=29")
                    }
                    .padding(top = 12.dp)
            )
        }
    }
}