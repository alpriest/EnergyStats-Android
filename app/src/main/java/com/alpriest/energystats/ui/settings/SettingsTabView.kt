package com.alpriest.energystats.ui.settings

import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SettingsTabView(
    navController: NavHostController,
    config: ConfigManaging,
    onLogout: () -> Unit,
    onRateApp: () -> Unit,
    onBuyMeCoffee: () -> Unit
) {
    val currentDevice = config.currentDevice.collectAsState()
    val uriHandler = LocalUriHandler.current

    SettingsPage {
        SettingsColumn(header = "Settings") {
            config.powerStationDetail?.let {
                InlineSettingsNavButton(stringResource(R.string.settings_power_station)) { navController.navigate(SettingsScreen.PowerStation.name) }
                Divider()
            }

            InlineSettingsNavButton(stringResource(R.string.inverter)) { navController.navigate(SettingsScreen.Inverter.name) }
            Divider()

            currentDevice.value?.let {
                if (it.battery != null) {
                    InlineSettingsNavButton(stringResource(R.string.battery)) { navController.navigate(SettingsScreen.Battery.name) }
                }
                Divider()
            }

            InlineSettingsNavButton("Dataloggers") { navController.navigate(SettingsScreen.Dataloggers.name) }
        }

        DisplaySettingsView(config, navController = navController)

        SettingsColumn {
            InlineSettingsNavButton("Data") { navController.navigate(SettingsScreen.DataSettings.name) }
            Divider()
            InlineSettingsNavButton(stringResource(R.string.self_sufficiency_estimates)) { navController.navigate(SettingsScreen.SelfSufficiencyEstimates.name) }
            Divider()
            InlineSettingsNavButton(stringResource(R.string.financial_model)) { navController.navigate(SettingsScreen.FinancialModel.name) }
            Divider()
            InlineSettingsNavButton(stringResource(R.string.solcast_solar_prediction)) { navController.navigate(SettingsScreen.SolcastSolarPrediction.name) }
        }

        SettingsColumn {
            InlineSettingsNavButton(
                title = stringResource(R.string.foxess_cloud_status),
                disclosureIcon = { Icons.Default.OpenInBrowser },
                onClick = { uriHandler.openUri("https://monitor.foxesscommunity.com/status/foxess") }
            )
            Divider()

            InlineSettingsNavButton(
                title = stringResource(R.string.foxess_community),
                disclosureIcon = { Icons.Default.OpenInBrowser },
                onClick = { uriHandler.openUri("https://www.foxesscommunity.com/") }
            )
            Divider()

            InlineSettingsNavButton(
                title = stringResource(R.string.facebook_group),
                disclosureIcon = { Icons.Default.OpenInBrowser },
                onClick = { uriHandler.openUri("https://www.facebook.com/groups/foxessownersgroup") }
            )
            Divider()

            InlineSettingsNavButton(
                title = stringResource(R.string.frequently_asked_questions),
                onClick = { navController.navigate(SettingsScreen.FAQ.name) }
            )
            Divider()

            InlineSettingsNavButton(
                title = stringResource(R.string.view_debug_data),
                onClick = { navController.navigate(SettingsScreen.Debug.name) }
            )
            Divider()

            InlineSettingsNavButton(
                title = stringResource(R.string.edit_api_key),
                onClick = { navController.navigate(SettingsScreen.APIKey.name) }
            )
        }

        SettingsFooterView(config, onLogout, onRateApp, onBuyMeCoffee)
    }
}

@Preview(showBackground = true, heightDp = 1200, widthDp = 400)
@Composable
fun SettingsViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        SettingsTabView(
            navController = NavHostController(LocalContext.current),
            config = FakeConfigManager(),
            onLogout = {},
            onRateApp = {}
        ) {}
    }
}