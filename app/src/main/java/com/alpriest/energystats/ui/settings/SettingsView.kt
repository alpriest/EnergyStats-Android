package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.services.InMemoryLoggingNetworkStore
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.battery.BatteryForceChargeTimes
import com.alpriest.energystats.ui.settings.battery.BatteryForceChargeTimesViewModel
import com.alpriest.energystats.ui.settings.battery.BatterySOCSettings
import com.alpriest.energystats.ui.settings.battery.BatterySettingsView
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SettingsColumnWithChild(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .background(colors.surface)
            .padding(10.dp)
    ) {
        content()
    }
}

enum class SettingsScreen() {
    Settings,
    Debug,
    Battery,
    BatterySOC,
    BatteryChargeTimes
}

@Composable
fun NavigableSettingsView(
    config: ConfigManaging,
    userManager: UserManaging,
    onLogout: () -> Unit,
    networkStore: InMemoryLoggingNetworkStore,
    onRateApp: () -> Unit,
    onSendUsEmail: () -> Unit,
    onBuyMeCoffee: () -> Unit,
    network: Networking
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SettingsScreen.Settings.name
    ) {
        composable(SettingsScreen.Settings.name) {
            SettingsView(
                navController,
                config = config,
                userManager = userManager,
                onLogout = onLogout,
                onRateApp = onRateApp,
                onSendUsEmail = onSendUsEmail,
                onBuyMeCoffee = onBuyMeCoffee
            )
        }
        composable(SettingsScreen.Battery.name) {
            BatterySettingsView(
                navController = navController,
                config = config
            )
        }
        composable(SettingsScreen.BatterySOC.name) {
            BatterySOCSettings(configManager = config, network = network, navController = navController).Content()
        }
        composable(SettingsScreen.BatteryChargeTimes.name) {
            BatteryForceChargeTimes(configManager = config, network = network, navController = navController).Content()
        }
        debugGraph(navController, networkStore)
    }
}

@Composable
fun SettingsButton(title: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .indication(interactionSource, rememberRipple())
    ) {
        Text(
            title,
            color = MaterialTheme.colors.onPrimary
        )
    }
}

@Composable
fun SettingsView(
    navController: NavHostController,
    config: ConfigManaging,
    userManager: UserManaging,
    onLogout: () -> Unit,
    onRateApp: () -> Unit,
    onSendUsEmail: () -> Unit,
    onBuyMeCoffee: () -> Unit
) {
    val scrollState = rememberScrollState()
    val currentDevice = config.currentDevice.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        InverterChoiceView(config = config)

        FirmwareVersionView(config)

        currentDevice.value?.let {
            if (it.battery != null) {
                SettingsButton("Battery") { navController.navigate(SettingsScreen.Battery.name) }
            }
        }

        DisplaySettingsView(config)

        SelfSufficiencySettingsView(config, modifier = Modifier.fillMaxWidth())

        RefreshFrequencySettingsView(config)

        SettingsButton(
            title = stringResource(R.string.view_debug_data),
            onClick = { navController.navigate(SettingsScreen.Debug.name) }
        )

        SettingsFooterView(config, userManager, onLogout, onRateApp, onSendUsEmail, onBuyMeCoffee)
    }
}

@Preview(showBackground = true, heightDp = 1200, widthDp = 300)
@Composable
fun SettingsViewPreview() {
    EnergyStatsTheme {
        SettingsView(
            navController = NavHostController(LocalContext.current),
            config = FakeConfigManager(),
            userManager = FakeUserManager(),
            onLogout = {},
            onRateApp = {},
            onSendUsEmail = {}
        ) {}
    }
}