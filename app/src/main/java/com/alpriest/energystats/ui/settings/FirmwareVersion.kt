package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.battery.SettingsTitleView

@Composable
fun FirmwareVersionView(config: ConfigManaging) {
    val uriHandler = LocalUriHandler.current
    val currentDevice = config.currentDevice.collectAsState()

    currentDevice.value?.firmware?.let {
        SettingsColumnWithChild {
            SettingsTitleView(stringResource(R.string.firmware_versions))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Column { Text("Manager: " + it.manager, color = colors.onSecondary) }
                Column { Text("Slave: " + it.slave, color = colors.onSecondary) }
                Column { Text("Master: " + it.master, color = colors.onSecondary) }
            }

            Text(
                text = stringResource(R.string.find_out_more_about_firmware_versions_from_the_foxesscommunity_com_website),
                color = colors.onSecondary,
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://foxesscommunity.com/viewforum.php?f=29")
                }
            )
        }
    }
}