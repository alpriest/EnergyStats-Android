package com.alpriest.energystats.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.services.trackScreenView
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.models.ColorThemeMode
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun SettingsTabView(
    navController: NavHostController,
    configManager: ConfigManaging,
    onLogout: suspend () -> Unit,
    onRateApp: () -> Unit,
    onBuyMeCoffee: () -> Unit,
    modifier: Modifier,
) {
    val currentDevice = configManager.currentDevice.collectAsState()
    val uriHandler = LocalUriHandler.current
    trackScreenView("Settings", "SettingsTabView")

    SettingsPage(modifier) {
        SettingsColumn {
            configManager.powerStationDetail?.let {
                InlineSettingsNavButton(stringResource(R.string.settings_power_station)) { navController.navigate(SettingsScreen.PowerStation.name) }
                HorizontalDivider()
            }

            InlineSettingsNavButton(stringResource(R.string.inverter)) { navController.navigate(SettingsScreen.Inverter.name) }
            HorizontalDivider()

            currentDevice.value?.let {
                if (it.battery != null) {
                    InlineSettingsNavButton(stringResource(R.string.battery)) { navController.navigate(SettingsScreen.Battery.name) }
                    HorizontalDivider()
                }
            }

            InlineSettingsNavButton(stringResource(R.string.dataloggers)) { navController.navigate(SettingsScreen.Dataloggers.name) }
        }

        DisplaySettingsView(configManager, navController = navController)

        SettingsColumn {
            InlineSettingsNavButton(stringResource(R.string.settings_data)) { navController.navigate(SettingsScreen.DataSettings.name) }
            HorizontalDivider()
            InlineSettingsNavButton(stringResource(R.string.self_sufficiency_estimates)) { navController.navigate(SettingsScreen.SelfSufficiencyEstimates.name) }
            HorizontalDivider()
            InlineSettingsNavButton(stringResource(R.string.financial_model)) { navController.navigate(SettingsScreen.FinancialModel.name) }
            HorizontalDivider()
            InlineSettingsNavButton(stringResource(R.string.solcast_solar_prediction)) { navController.navigate(SettingsScreen.SolcastSolarPrediction.name) }
        }

        SettingsColumn {
            val mode = configManager.appSettingsStream.collectAsState().value.isReadOnly.asOnOff()
            InlineSettingsNavButton(
                title = stringResource(R.string.read_only_mode_title, mode),
                onClick = { navController.navigate(SettingsScreen.ReadOnlyModeSettings.name)  }
            )
            HorizontalDivider()

            InlineSettingsNavButton(
                title = stringResource(R.string.foxess_cloud_status),
                disclosureIcon = { Icons.Default.OpenInBrowser },
                onClick = { uriHandler.openUri("https://monitor.foxesscommunity.com/status/foxess") }
            )
            HorizontalDivider()

            InlineSettingsNavButton(
                title = stringResource(R.string.view_debug_data),
                onClick = { navController.navigate(SettingsScreen.Debug.name) }
            )
            HorizontalDivider()

            InlineSettingsNavButton(
                title = stringResource(R.string.edit_api_key),
                onClick = { navController.navigate(SettingsScreen.APIKey.name) }
            )
            HorizontalDivider()

            ReloadDevicesButton(configManager)

            HorizontalDivider()

            InlineSettingsNavButton(
                title = stringResource(R.string.reset_app_settings),
                onClick = { navController.navigate(SettingsScreen.FactoryResetAppSettings.name) }
            )

            HorizontalDivider()

            InlineSettingsNavButton(
                title = stringResource(R.string.contact),
                onClick = { navController.navigate(SettingsScreen.Contact.name) }
            )
        }

        SettingsFooterView(configManager, onLogout, onRateApp, onBuyMeCoffee)
    }
}

@Preview(showBackground = true, heightDp = 1200, widthDp = 400)
@Composable
fun SettingsViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        SettingsTabView(
            navController = NavHostController(LocalContext.current),
            configManager = FakeConfigManager(),
            onLogout = {},
            onRateApp = {},
            {},
            Modifier
        )
    }
}