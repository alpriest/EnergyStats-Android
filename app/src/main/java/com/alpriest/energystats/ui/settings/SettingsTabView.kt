package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsTabView(
    navController: NavHostController,
    config: ConfigManaging,
    onLogout: () -> Unit,
    onRateApp: () -> Unit,
    onBuyMeCoffee: () -> Unit,
    modifier: Modifier,
) {
    val currentDevice = config.currentDevice.collectAsState()
    val uriHandler = LocalUriHandler.current

    SettingsPage(modifier) {
        SettingsColumn {
            config.powerStationDetail?.let {
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

        DisplaySettingsView(config, navController = navController)

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
            InlineSettingsNavButton(
                title = stringResource(R.string.foxess_cloud_status),
                disclosureIcon = { Icons.Default.OpenInBrowser },
                onClick = { uriHandler.openUri("https://monitor.foxesscommunity.com/status/foxess") }
            )
            HorizontalDivider()

            InlineSettingsNavButton(
                title = stringResource(R.string.foxess_community),
                disclosureIcon = { Icons.Default.OpenInBrowser },
                onClick = { uriHandler.openUri("https://www.foxesscommunity.com/") }
            )
            HorizontalDivider()

            InlineSettingsNavButton(
                title = stringResource(R.string.facebook_group),
                disclosureIcon = { Icons.Default.OpenInBrowser },
                onClick = { uriHandler.openUri("https://www.facebook.com/groups/foxessownersgroup") }
            )
            HorizontalDivider()

            InlineSettingsNavButton(
                title = stringResource(R.string.frequently_asked_questions),
                onClick = { navController.navigate(SettingsScreen.FAQ.name) }
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

            ReloadDevicesButton(config)
        }

        SettingsFooterView(config, onLogout, onRateApp, onBuyMeCoffee)
    }
}

@Composable
fun ReloadDevicesButton(config: ConfigManaging) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    InlineSettingsNavButton(
        title = "Reload devices from FoxESS Cloud",
        disclosureIcon = null,
        disclosureView = {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Tap to refresh",
                    modifier = Modifier.padding(end = 12.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        },
        onClick = {
            scope.launch {
                try {
                    isLoading = true
                    config.fetchDevices()
                    isLoading = false
                } catch (ex: Exception) {
                    isLoading = false
                }
            }
        }
    )
}

@Preview(showBackground = true, heightDp = 1200, widthDp = 400)
@Composable
fun SettingsViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        SettingsTabView(
            navController = NavHostController(LocalContext.current),
            config = FakeConfigManager(),
            onLogout = {},
            onRateApp = {},
            {},
            Modifier
        )
    }
}