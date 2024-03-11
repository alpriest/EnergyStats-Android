package com.alpriest.energystats.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.services.InMemoryLoggingNetworkStore
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.CredentialStore
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.battery.BatteryChargeScheduleSettingsView
import com.alpriest.energystats.ui.settings.battery.BatterySOCSettings
import com.alpriest.energystats.ui.settings.battery.BatterySettingsView
import com.alpriest.energystats.ui.settings.dataloggers.DataLoggerViewContainer
import com.alpriest.energystats.ui.settings.financial.FinancialsSettingsView
import com.alpriest.energystats.ui.settings.inverter.InverterSettingsView
import com.alpriest.energystats.ui.settings.inverter.schedule.EditPhaseView
import com.alpriest.energystats.ui.settings.inverter.schedule.EditScheduleView
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleSummaryView
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.EditTemplateView
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.ScheduleTemplateListView
import com.alpriest.energystats.ui.settings.solcast.SolarForecasting
import com.alpriest.energystats.ui.settings.solcast.SolcastSettingsView

@Composable
fun NavigableSettingsView(
    config: ConfigManaging,
    userManager: UserManaging,
    onLogout: () -> Unit,
    networkStore: InMemoryLoggingNetworkStore,
    onRateApp: () -> Unit,
    onBuyMeCoffee: () -> Unit,
    network: Networking,
    credentialStore: CredentialStore,
    solarForecastingProvider: () -> SolarForecasting
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
                onLogout = onLogout,
                onRateApp = onRateApp,
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
            BatterySOCSettings(configManager = config, network = network, navController = navController, userManager = userManager).Content()
        }
        composable(SettingsScreen.BatteryChargeTimes.name) {
            BatteryChargeScheduleSettingsView(configManager = config, network = network, navController = navController, userManager = userManager).Content()
        }
        composable(SettingsScreen.Inverter.name) {
            InverterSettingsView(configManager = config, network = network, navController = navController)
        }
        composable(SettingsScreen.InverterSchedule.name) {
            ScheduleSummaryView(config, network, navController, userManager).Content()
        }
        composable(SettingsScreen.Dataloggers.name) {
            DataLoggerViewContainer(network = network, configManager = config, navController = navController, context = context).Content()
        }
        composable(SettingsScreen.FinancialModel.name) {
            SettingsPage {
                FinancialsSettingsView(config)
            }
        }
        composable(SettingsScreen.SelfSufficiencyEstimates.name) {
            SettingsPage {
                SelfSufficiencySettingsView(config)
            }
        }
        composable(SettingsScreen.SolarBandings.name) {
            SolarBandingSettingsView(navController, config)
        }
        composable(SettingsScreen.FAQ.name) {
            FAQView()
        }
        composable(SettingsScreen.SolcastSolarPrediction.name) {
            SolcastSettingsView(navController, config, userManager, solarForecastingProvider).Content()
        }
        composable(SettingsScreen.Summary.name) {
            ScheduleSummaryView(config, network, navController, userManager).Content()
        }

        composable(SettingsScreen.EditSchedule.name) {
            EditScheduleView(
                config,
                network,
                navController,
                userManager
            ).Content()
        }

        composable(SettingsScreen.EditPhase.name) {
            EditPhaseView(navController)
        }

        composable(SettingsScreen.TemplateList.name) {
            ScheduleTemplateListView(config, network, navController, userManager).Content()
        }

        composable(SettingsScreen.EditTemplate.name) {
            EditTemplateView(config, network, navController, userManager).Content()
        }

        composable(SettingsScreen.APIKey.name) {
            ConfigureAPIKeyView(userManager.store, navController, config.themeStream)
        }

        composable(SettingsScreen.PowerStation.name) {
            PowerStationView(config, navController)
        }

        composable(SettingsScreen.DataSettings.name) {
            DataSettingsView(config)
        }

        debugGraph(navController, networkStore, config, network, credentialStore)
    }
}
