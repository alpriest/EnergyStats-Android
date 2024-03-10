package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.preview.FakeUserManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

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
                color = colors.onPrimary
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
    onBuyMeCoffee: () -> Unit
) {
    val currentDevice = config.currentDevice.collectAsState()
    val uriHandler = LocalUriHandler.current

    SettingsPage {
        SettingsButtonList {
            SettingsNavButton(stringResource(R.string.inverter)) { navController.navigate(SettingsScreen.Inverter.name) }

            currentDevice.value?.let {
                if (it.battery != null) {
                    SettingsNavButton(stringResource(R.string.battery)) { navController.navigate(SettingsScreen.Battery.name) }
                }
            }

            SettingsNavButton("Dataloggers") { navController.navigate(SettingsScreen.Dataloggers.name) }
        }

        DisplaySettingsView(config)
        RefreshFrequencySettingsView(config)

        Column {
            SettingsNavButton(stringResource(R.string.self_sufficiency_estimates)) { navController.navigate(SettingsScreen.SelfSufficiencyEstimates.name) }
            SettingsNavButton(stringResource(R.string.financial_model)) { navController.navigate(SettingsScreen.FinancialModel.name) }
            SettingsNavButton(stringResource(R.string.sun_display_variation_thresholds)) { navController.navigate(SettingsScreen.SolarBandings.name) }
            SettingsNavButton(stringResource(R.string.solcast_solar_prediction)) { navController.navigate(SettingsScreen.SolcastSolarPrediction.name) }
        }

        Column {
            SettingsNavButton(
                title = stringResource(R.string.foxess_cloud_status),
                disclosureIcon = { Icons.Default.OpenInBrowser },
                onClick = { uriHandler.openUri("https://monitor.foxesscommunity.com/status/foxess") }
            )

            SettingsNavButton(
                title = stringResource(R.string.foxess_community),
                disclosureIcon = { Icons.Default.OpenInBrowser },
                onClick = { uriHandler.openUri("https://www.foxesscommunity.com/") }
            )

            SettingsNavButton(
                title = stringResource(R.string.facebook_group),
                disclosureIcon = { Icons.Default.OpenInBrowser },
                onClick = { uriHandler.openUri("https://www.facebook.com/groups/foxessownersgroup") }
            )

            SettingsNavButton(
                title = stringResource(R.string.frequently_asked_questions),
                onClick = { navController.navigate(SettingsScreen.FAQ.name) }
            )

            SettingsNavButton(
                title = stringResource(R.string.view_debug_data),
                onClick = { navController.navigate(SettingsScreen.Debug.name) }
            )

            SettingsNavButton(
                title = stringResource(R.string.edit_api_key),
                onClick = { navController.navigate(SettingsScreen.APIKey.name) }
            )
        }

        SettingsFooterView(config, onLogout, onRateApp, onBuyMeCoffee)
    }
}

@Preview(showBackground = true, heightDp = 1200, widthDp = 300)
@Composable
fun SettingsViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        SettingsTabView(
            navController = NavHostController(LocalContext.current),
            config = FakeConfigManager(),
            userManager = FakeUserManager(),
            onLogout = {},
            onRateApp = {}
        ) {}
    }
}