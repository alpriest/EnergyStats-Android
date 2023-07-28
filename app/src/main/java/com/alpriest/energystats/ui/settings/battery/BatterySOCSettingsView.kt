package com.alpriest.energystats.ui.settings.battery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.SettingsButton
import com.alpriest.energystats.ui.settings.SettingsColumnWithChild
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun BatterySOCSettingsView(config: ConfigManaging, network: Networking) {
    var minSOC by rememberSaveable { mutableStateOf("20") }
    var minSOConGrid by rememberSaveable { mutableStateOf("20") }

    SettingsColumnWithChild(
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Min SoC",
                        Modifier.weight(1.0f),
                        style = MaterialTheme.typography.h4
                    )
                    OutlinedTextField(
                        value = minSOC,
                        onValueChange = { minSOC = it },
                        modifier = Modifier.width(100.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                        trailingIcon = { Text("%") }
                    )
                }

                Text(
                    "The minimum charge the battery should maintain.",
                    color = MaterialTheme.colors.onSecondary
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Min SoC on Grid",
                        Modifier.weight(1.0f),
                        style = MaterialTheme.typography.h4
                    )
                    OutlinedTextField(
                        value = minSOConGrid,
                        onValueChange = { minSOConGrid = it },
                        modifier = Modifier.width(100.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                        trailingIcon = { Text("%") }
                    )
                }

                Text(
                    "The minimum charge the battery should maintain when grid power is present.",
                    color = MaterialTheme.colors.onSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    "For the most part this is the setting that determines when the batteries will stop being used. Setting this higher than Min SoC will reserve battery power for a grid outage. For example, if you set Min SoC to 10% and Min SoC on Grid to 20%, the inverter will stop supplying power from the batteries at 20% and the house load will be supplied from the grid. If there is a grid outage, the batteries could be used (via an EPS switch) to supply emergency power until the battery charge drops to 10%.",
                    color = MaterialTheme.colors.onSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    "If you're not sure then set both values the same.",
                    color = MaterialTheme.colors.onSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BatterySOCSettingsViewPreview() {
    EnergyStatsTheme {
        BatterySOCSettingsView(
            config = FakeConfigManager(),
            network = DemoNetworking()
        )
    }
}