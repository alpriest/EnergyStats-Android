package com.alpriest.energystats.ui.settings.inverter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.settings.SettingsTitleView

@Composable
fun SettingsRow(title: String, value: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text(title,
            color = colors.onSecondary,
            style = TextStyle.Default.copy(fontWeight = FontWeight.Medium)
        )
        Text(value ?: "(unknown)", color = colors.onSecondary)
    }
}

@Composable
fun FirmwareVersionView(device: Device) {
    val uriHandler = LocalUriHandler.current

    device.firmware?.let {
        SettingsColumnWithChild {
            SettingsTitleView(stringResource(R.string.firmware_versions))

            SettingsRow("Manager", it.manager)
            SettingsRow("Slave", it.slave)
            SettingsRow("Master", it.master)

            Text(
                text = stringResource(R.string.find_out_more_about_firmware_versions_from_the_foxesscommunity_com_website),
                color = colors.onSecondary,
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://foxesscommunity.com/viewforum.php?f=29")
                }.padding(top = 12.dp)
            )
        }
    }
}