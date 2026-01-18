package com.alpriest.energystats.ui.settings.inverter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.models.DeviceFirmwareVersion
import com.alpriest.energystats.shared.network.DemoNetworking
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.ui.LoadingView
import com.alpriest.energystats.ui.flow.preview
import com.alpriest.energystats.ui.settings.SettingsColumn
import com.alpriest.energystats.ui.theme.ESButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsRow(title: String, value: String?) {
    SettingsRow(title) {
        Text(
            value ?: "(unknown)",
            color = colorScheme.onSecondary,
            modifier = Modifier.padding(end = 4.dp)
        )
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
            color = colorScheme.onSecondary,
            modifier = Modifier.padding(end = 8.dp)
        )
        content()
    }
}

@Composable
fun FirmwareVersionView(device: Device, network: Networking) {
    val uriHandler = LocalUriHandler.current
    var firmware: DeviceFirmwareVersion? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    firmware?.let {
        SettingsColumn(
            header = stringResource(R.string.firmware_versions),
            footer = stringResource(R.string.find_out_more),
            footerModifier = Modifier
                .clickable {
                    uriHandler.openUri("https://foxesscommunity.com/viewforum.php?f=29")
                },
        ) {
            SettingsRow("Manager", it.manager)
            SettingsRow("Slave", it.slave)
            SettingsRow("Master", it.master)
        }
    } ?: SettingsColumn {
        if (isLoading) {
            LoadingView(title = stringResource(R.string.loading), stringResource(R.string.still_loading))
        } else {
            ESButton(
                onClick = {
                    scope.launch {
                        try {
                            isLoading = true
                            val response = network.fetchDevice(device.deviceSN)
                            firmware = DeviceFirmwareVersion(manager = response.managerVersion, slave = response.slaveVersion, master = response.masterVersion)
                            isLoading = false
                        } catch (ex: Exception) {
                            isLoading = false
                        }
                    }
                }
            ) {
                Text("Tap to load firmware versions")
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun StatsGraphViewPreview() {
    EnergyStatsTheme {
        Surface {
            FirmwareVersionView(device = Device.preview(), network = DemoNetworking())
        }
    }
}
