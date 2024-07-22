package com.alpriest.energystats.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.R
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
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.TemplateStore
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
    val templateStore = TemplateStore(config) // TODO inject this

    NavHost(
        navController = navController,
        startDestination = SettingsScreen.Settings.name
    ) {
        composable(SettingsScreen.Settings.name) {
            LoadedScaffold(title = "Settings") {
                SettingsTabView(
                    navController,
                    config = config,
                    onLogout = onLogout,
                    onRateApp = onRateApp,
                    onBuyMeCoffee = onBuyMeCoffee
                )
            }
        }
        composable(SettingsScreen.Battery.name) {
            LoadedScaffold(title = "Battery", navController = navController) {
                BatterySettingsView(
                    navController = navController,
                    config = config
                )
            }
        }
        composable(SettingsScreen.BatterySOC.name) {
            LoadedScaffold(title = "Battery SOC", navController = navController) {
                BatterySOCSettings(configManager = config, network = network, navController = navController, userManager = userManager).Content()
            }
        }
        composable(SettingsScreen.BatteryChargeTimes.name) {
            LoadedScaffold(title = "Battery Charge Schedule", navController = navController) {
                BatteryChargeScheduleSettingsView(configManager = config, network = network, navController = navController, userManager = userManager).Content()
            }
        }
        composable(SettingsScreen.Inverter.name) {
            LoadedScaffold(title = "Inverter", navController = navController) {
                InverterSettingsView(configManager = config, network = network, navController = navController)
            }
        }
        composable(SettingsScreen.InverterSchedule.name) {
            LoadedScaffold(title = "Schedules", navController = navController) {
                ScheduleSummaryView(config, network, navController, userManager, templateStore).Content()
            }
        }
        composable(SettingsScreen.Dataloggers.name) {
            LoadedScaffold(title = stringResource(R.string.dataloggers), navController = navController) {
                DataLoggerViewContainer(network = network, configManager = config, navController = navController).Content()
            }
        }
        composable(SettingsScreen.FinancialModel.name) {
            LoadedScaffold(title = stringResource(R.string.financial_model), navController = navController) {
                SettingsPage {
                    FinancialsSettingsView(config)
                }
            }
        }
        composable(SettingsScreen.SelfSufficiencyEstimates.name) {
            LoadedScaffold(title = stringResource(R.string.self_sufficiency_estimates), navController = navController) {
                SettingsPage {
                    SelfSufficiencySettingsView(config)
                }
            }
        }
        composable(SettingsScreen.SolarBandings.name) {
            LoadedScaffold(title = stringResource(R.string.sun_display_variation_thresholds), navController = navController) {
                SolarBandingSettingsView(navController, config)
            }
        }
        composable(SettingsScreen.FAQ.name) {
            FAQView()
        }
        composable(SettingsScreen.SolcastSolarPrediction.name) {
            LoadedScaffold(title = stringResource(R.string.solcast_solar_prediction), navController = navController) {
                SolcastSettingsView(navController, config, userManager, solarForecastingProvider).Content()
            }
        }
        composable(SettingsScreen.ScheduleSummary.name) {
            ScheduleSummaryView(config, network, navController, userManager, templateStore).Content()
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
            LoadedScaffold(title = stringResource(R.string.edit_phase), navController = navController) {
                EditPhaseView(navController)
            }
        }

        composable(SettingsScreen.TemplateList.name) {
            LoadedScaffold(title = stringResource(R.string.templates), navController = navController) {
                ScheduleTemplateListView(config, templateStore, navController, userManager).Content()
            }
        }

        composable(SettingsScreen.EditTemplate.name) {
            EditTemplateView(config, network, navController, userManager, templateStore).Content()
        }

        composable(SettingsScreen.APIKey.name) {
            LoadedScaffold(title = stringResource(R.string.edit_api_key), navController = navController) {
                ConfigureAPIKeyView(userManager.store, navController, config.themeStream)
            }
        }

        composable(SettingsScreen.PowerStation.name) {
            config.powerStationDetail?.let {
                LoadedScaffold(title = stringResource(R.string.settings_power_station), navController = navController) {
                    PowerStationSettingsView(it)
                }
            }
        }

        composable(SettingsScreen.DataSettings.name) {
            LoadedScaffold(title = stringResource(R.string.settings_data), navController = navController) {
                DataSettingsView(config)
            }
        }

        debugGraph(navController, networkStore, config, network, credentialStore)
    }
}
