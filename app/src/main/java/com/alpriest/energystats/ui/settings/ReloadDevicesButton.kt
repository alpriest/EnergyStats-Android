package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.config.ConfigManaging
import kotlinx.coroutines.launch

@Composable
fun ReloadDevicesButton(configManager: ConfigManaging) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    InlineSettingsNavButton(
        title = stringResource(R.string.reload_devices_from_foxess_cloud),
        disclosureIcon = null,
        disclosureView = {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.Companion.size(22.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Tap to refresh",
                    modifier = Modifier.Companion.padding(end = 12.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        },
        onClick = {
            scope.launch {
                try {
                    isLoading = true
                    configManager.fetchDevices()
                    configManager.fetchPowerStationDetail()
                    isLoading = false
                } catch (ex: Exception) {
                    isLoading = false
                }
            }
        }
    )
}