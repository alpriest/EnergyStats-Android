package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SettingsView(config: ConfigManaging, userManager: UserManaging, onLogout: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        BatterySettingsView(
            config = config
        )

        Divider()

        DisplaySettings(
            config = config
        )

        Divider()

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = CenterHorizontally
        ) {
            userManager.getUsername()?.let {
                Text(
                    modifier = Modifier.padding(bottom = 24.dp),
                    text = "You are logged in as $it"
                )
            }

            Button(onClick = onLogout) {
                Text(
                    "Logout",
                    color = colors.onPrimary
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
fun SettingsViewPreview() {
    EnergyStatsTheme {
        SettingsView(
            config = FakeConfigManager(),
            userManager = FakeUserManager(),
            onLogout = {}
        )
    }
}