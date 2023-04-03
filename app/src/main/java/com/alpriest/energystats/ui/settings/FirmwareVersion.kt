package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.DeviceFirmwareVersion
import com.alpriest.energystats.stores.ConfigManaging
import kotlinx.coroutines.launch

@Composable
fun FirmwareVersionView(config: ConfigManaging) {
    var firmwareVersion by remember { mutableStateOf<DeviceFirmwareVersion?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    Column {
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

        error?.let {
            Text(it)
        }

        Divider(modifier = Modifier.padding(top = 24.dp))
    }.run {
        coroutineScope.launch {
            try {
                firmwareVersion = config.fetchFirmwareVersions()
            } catch (ex: Exception) {
                error = ex.localizedMessage
            }
        }
    }
}