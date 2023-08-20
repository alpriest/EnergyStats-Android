package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.alpriest.energystats.ui.settings.battery.BatterySOCSettings
import com.alpriest.energystats.ui.settings.battery.BatteryScheduleTimes
import com.alpriest.energystats.ui.settings.battery.BatterySettingsView
import com.alpriest.energystats.ui.settings.dataloggers.DataLoggerViewContainer
import com.alpriest.energystats.ui.settings.inverter.InverterSettingsView
import com.alpriest.energystats.ui.settings.inverter.WorkModeView
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
    BatteryChargeTimes,
    Inverter,
    InverterWorkMode,
    Dataloggers,
    Approximations
}

@Composable
fun NavigableSettingsView(
    config: ConfigManaging,
    userManager: UserManaging,
    onLogout: () -> Unit,
    networkStore: InMemoryLoggingNetworkStore,
    onRateApp: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onBuyMeCoffee: () -> Unit,
    network: Networking
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = SettingsScreen.Settings.name
    ) {
        composable(SettingsScreen.Settings.name) {
            SettingsTabView(
                navController,
                config = config,
                userManager = userManager,
                onLogout = onLogout,
                onRateApp = onRateApp,
                onOpenUrl = onOpenUrl,
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
            BatterySOCSettings(configManager = config, network = network, navController = navController, context = context).Content()
        }
        composable(SettingsScreen.BatteryChargeTimes.name) {
            BatteryScheduleTimes(configManager = config, network = network, navController = navController, context = context).Content()
        }
        composable(SettingsScreen.Inverter.name) {
            InverterSettingsView(configManager = config, navController = navController)
        }
        composable(SettingsScreen.InverterWorkMode.name) {
            WorkModeView(configManager = config, network = network, navController = navController, context = context).Content()
        }
        composable(SettingsScreen.Dataloggers.name) {
            DataLoggerViewContainer(network = network, configManager = config, navController = navController, context = context).Content()
        }
        composable(SettingsScreen.Approximations.name) {
            ApproximationsView(config)
        }
        debugGraph(navController, networkStore, config, network)
    }
}

@Composable
fun SettingsNavButton(title: String, modifier: Modifier = Modifier, disclosureIcon: (() -> ImageVector)? = { Icons.Default.ChevronRight }, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .indication(interactionSource, rememberRipple())
    ) {
        Row(
            modifier = if (disclosureIcon == null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
            ) {
            Text(
                title,
                color = MaterialTheme.colors.onPrimary
            )

            disclosureIcon?.let {
                Icon(
                    imageVector = it(),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SettingsTabView(
    navController: NavHostController,
    config: ConfigManaging,
    userManager: UserManaging,
    onLogout: () -> Unit,
    onRateApp: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onBuyMeCoffee: () -> Unit
) {
    val currentDevice = config.currentDevice.collectAsState()

    SettingsPage {
        Column {
            SettingsNavButton(stringResource(R.string.inverter)) { navController.navigate(SettingsScreen.Inverter.name) }

            currentDevice.value?.let {
                if (it.battery != null) {
                    SettingsNavButton(stringResource(R.string.battery)) { navController.navigate(SettingsScreen.Battery.name) }
                }
            }

            SettingsNavButton("Dataloggers") { navController.navigate(SettingsScreen.Dataloggers.name) }
        }

        DisplaySettingsView(config, navController)

        RefreshFrequencySettingsView(config)

        Column {
            SettingsNavButton(
                title = "FoxESS Cloud Status",
                onClick = { onOpenUrl("https://monitor.foxesscommunity.com/status/foxess") }
            )

            SettingsNavButton(
                title = "Facebook group",
                disclosureIcon = { Icons.Default.OpenInBrowser },
                onClick = { onOpenUrl("https://www.facebook.com/groups/foxessownersgroup") }
            )

            SettingsNavButton(
                title = stringResource(R.string.view_debug_data),
                onClick = { navController.navigate(SettingsScreen.Debug.name) }
            )
        }

        SettingsFooterView(config, userManager, onLogout, onRateApp, onOpenUrl, onBuyMeCoffee)
    }
}

@Preview(showBackground = true, heightDp = 1200, widthDp = 300)
@Composable
fun SettingsViewPreview() {
    EnergyStatsTheme(darkTheme = true) {
        SettingsTabView(
            navController = NavHostController(LocalContext.current),
            config = FakeConfigManager(),
            userManager = FakeUserManager(),
            onLogout = {},
            onRateApp = {},
            onOpenUrl = {}
        ) {}
    }
}