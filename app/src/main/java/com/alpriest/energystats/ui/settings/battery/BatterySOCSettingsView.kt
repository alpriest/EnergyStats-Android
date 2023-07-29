package com.alpriest.energystats.ui.settings.battery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.DemoNetworking
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.SettingsButton
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.launch

class BatterySOCSettings(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val navController: NavController
) {
    @Composable
    fun Content(viewModel: BatterySOCSettingsViewModel = viewModel(factory = BatterySOCSettingsViewModelFactory(network, configManager, navController))) {
        val minSOC = viewModel.minSOCStream.collectAsState().value
        val minSOConGrid = viewModel.minSOConGridStream.collectAsState().value
        val isActive = viewModel.activityStream.collectAsState().value
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(null) {
            viewModel.load()
        }

        isActive?.let {
            Column(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                androidx.compose.material.Text(it)
            }
        } ?: run {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(MaterialTheme.colors.surface)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Min SoC",
                            Modifier.weight(1.0f),
                            style = MaterialTheme.typography.h4
                        )
                        OutlinedTextField(
                            value = minSOC,
                            onValueChange = { viewModel.minSOCStream.value = it },
                            modifier = Modifier.width(100.dp),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                            trailingIcon = { Text("%") }
                        )
                    }

                    Text(
                        "The minimum charge the battery should maintain.",
                        color = MaterialTheme.colors.onSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .background(MaterialTheme.colors.surface)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Min SoC on Grid",
                            Modifier.weight(1.0f),
                            style = MaterialTheme.typography.h4
                        )
                        OutlinedTextField(
                            value = minSOConGrid,
                            onValueChange = { viewModel.minSOConGridStream.value = it },
                            modifier = Modifier.width(100.dp),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                            trailingIcon = { Text("%") }
                        )
                    }

                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
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

                SettingsButton("Save") {
                    coroutineScope.launch {
                        viewModel.save()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BatterySOCSettingsViewPreview() {
    EnergyStatsTheme {
        BatterySOCSettings(
            configManager = FakeConfigManager(),
            network = DemoNetworking(),
            navController = NavHostController(LocalContext.current)
        ).Content()
    }
}
