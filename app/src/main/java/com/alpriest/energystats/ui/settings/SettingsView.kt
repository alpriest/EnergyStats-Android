package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.models.asPercent
import com.alpriest.energystats.models.kW
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SettingsView(config: ConfigManaging, userManager: UserManaging, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .padding(12.dp),
        horizontalAlignment = CenterHorizontally
    ) {
        Row {
            Text(
                modifier = Modifier.padding(start = 12.dp),
                text = "Battery",
                style = MaterialTheme.typography.h4
            )
            Spacer(Modifier.weight(1f))
        }

        RoundedBox {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text("Min SOC")
                    Spacer(Modifier.weight(1f))
                    Text(text = config.minSOC.asPercent())
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text("Capacity")
                    Spacer(Modifier.weight(1f))
                    Text(text = config.batteryCapacityW.kW())
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            modifier = Modifier.padding(horizontal = 12.dp),
            text = "These values are automatically calculated from your installation. If your battery is below min SOC then the total capacity calculation will be incorrect.",
            style = MaterialTheme.typography.caption
        )

        Text(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp),
            text = "Empty/full durations are estimates based on calculated capacity, assume that solar conditions and battery charge rates remain constant.",
            style = MaterialTheme.typography.caption
        )

        Spacer(Modifier.height(24.dp))

        RoundedBox {
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
                    Text("Logout")
                }
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