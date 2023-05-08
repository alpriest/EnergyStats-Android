package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.models.RawDataStore
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SettingsFooterView(config: ConfigManaging, userManager: UserManaging, onLogout: () -> Unit, onRateApp: () -> Unit, onSendUsEmail: () -> Unit) {
    Column(
        Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        userManager.getUsername()?.let {
            Text(
                modifier = Modifier.padding(bottom = 24.dp), text = "You are logged in as $it"
            )
        }

        Button(onClick = onLogout) {
            Text(
                "Logout", color = MaterialTheme.colors.onPrimary
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onRateApp, modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.ThumbUp, contentDescription = "Thumbs Up", modifier = Modifier.padding(end = 5.dp)
                )
                Text(
                    text = "Rate this app",
                    fontSize = 12.sp,
                )
            }

            Spacer(modifier = Modifier.widthIn(min = 20.dp))

            OutlinedButton(
                onClick = onSendUsEmail, modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Email, contentDescription = "Email", modifier = Modifier.padding(end = 5.dp)
                )
                Text(
                    "Get in touch",
                    fontSize = 12.sp,
                )
            }
        }

        Text(
            "Version " + config.appVersion,
            modifier = Modifier.padding(top = 44.dp),
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true, heightDp = 600, widthDp = 300)
@Composable
fun SettingsFooterViewPreview() {
    EnergyStatsTheme {
        SettingsFooterView(config = FakeConfigManager(), userManager = FakeUserManager(), onLogout = {}, onRateApp = {}, onSendUsEmail = {})
    }
}