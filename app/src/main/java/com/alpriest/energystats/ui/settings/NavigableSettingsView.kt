package com.alpriest.energystats.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.DeviceSettingsItem
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.tabs.TopBarSettings
import com.alpriest.energystats.ui.login.UserManaging
import com.alpriest.energystats.ui.settings.battery.BatteryChargeScheduleSettingsView
import com.alpriest.energystats.ui.settings.battery.BatterySOCSettings
import com.alpriest.energystats.ui.settings.battery.BatterySettingsView
import com.alpriest.energystats.ui.settings.dataloggers.DataLoggerViewContainer
import com.alpriest.energystats.ui.settings.devicesettings.DeviceSettingItemView
import com.alpriest.energystats.ui.settings.financial.FinancialsSettingsView
import com.alpriest.energystats.ui.settings.inverter.InverterSettingsView
import com.alpriest.energystats.ui.settings.inverter.WorkModeSettingsView
import com.alpriest.energystats.ui.settings.inverter.schedule.EditPhaseView
import com.alpriest.energystats.ui.settings.inverter.schedule.EditScheduleView
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleSummaryView
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.EditTemplateView
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.ScheduleTemplateListView
import com.alpriest.energystats.ui.settings.inverter.schedule.templates.TemplateStoring
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.alpriest.energystats.ui.settings.solcast.SolcastSettingsView
import com.alpriest.energystats.ui.slideComposable
import com.example.energystats.FactoryResetAppSettingsView

@Composable
fun NavigableSettingsView(
    topBarSettings: MutableState<TopBarSettings>,
    configManager: ConfigManaging,
    userManager: UserManaging,
    onLogout: () -> Unit,
    onRateApp: () -> Unit,
    onBuyMeCoffee: () -> Unit,
    network: Networking,
    solarForecastingProvider: () -> SolcastCaching,
    templateStore: TemplateStoring
) {
    val lastSettingsResetTimeViewKey by remember { mutableStateOf(configManager.lastSettingsResetTime) }
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SettingsScreen.Settings.name
    ) {
        slideComposable(SettingsScreen.Settings.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.settings_tab), {}, null)
            key(lastSettingsResetTimeViewKey) {
                SettingsTabView(
                    navController,
                    config = configManager,
                    onLogout = onLogout,
                    onRateApp = onRateApp,
                    onBuyMeCoffee = onBuyMeCoffee,
                    modifier = Modifier
                )
            }
        }
        slideComposable(SettingsScreen.Battery.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.battery), {}, { navController.popBackStack() })
            BatterySettingsView(
                navController = navController,
                config = configManager,
                modifier = Modifier
            )
        }
        slideComposable(SettingsScreen.BatterySOC.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.battery_soc), {}, { navController.popBackStack() })
            BatterySOCSettings(configManager = configManager, network = network, navController = navController, userManager = userManager).Content(modifier = Modifier)
        }
        slideComposable(SettingsScreen.BatteryChargeTimes.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.battery_charge_schedule), {}, { navController.popBackStack() })
            BatteryChargeScheduleSettingsView(
                configManager = configManager,
                network = network,
                navController = navController,
                userManager = userManager
            ).Content(modifier = Modifier)
        }
        slideComposable(SettingsScreen.Inverter.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.inverter), {}, { navController.popBackStack() })
            InverterSettingsView(configManager = configManager, network = network, navController = navController, modifier = Modifier)
        }
        slideComposable(SettingsScreen.InverterSchedule.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.manage_schedules), {}, { navController.popBackStack() })
            ScheduleSummaryView(configManager, network, navController, userManager, templateStore).Content(modifier = Modifier)
        }
        slideComposable(SettingsScreen.Dataloggers.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.dataloggers), {}, { navController.popBackStack() })
            DataLoggerViewContainer(network = network, configManager = configManager, navController = navController).Content(modifier = Modifier)
        }
        slideComposable(SettingsScreen.FinancialModel.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.financial_model), {}, { navController.popBackStack() })
            SettingsPage(Modifier) {
                FinancialsSettingsView(configManager)
            }
        }
        slideComposable(SettingsScreen.SelfSufficiencyEstimates.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.self_sufficiency_estimates), {}, { navController.popBackStack() })
            SettingsPage(Modifier) {
                SelfSufficiencySettingsView(configManager)
            }
        }
        slideComposable(SettingsScreen.SolarBandings.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.sun_display_thresholds), {}, { navController.popBackStack() })
            SolarBandingSettingsView(navController, configManager, Modifier)
        }
        slideComposable(SettingsScreen.FAQ.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.frequently_asked_questions), {}, { navController.popBackStack() })
            FAQView()
        }
        slideComposable(SettingsScreen.SolcastSolarPrediction.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.solcast_solar_prediction), {}, { navController.popBackStack() })
            SolcastSettingsView(navController, configManager, userManager, solarForecastingProvider).Content(modifier = Modifier)
        }
        slideComposable(SettingsScreen.EditSchedule.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.edit_schedule), {}, { navController.popBackStack() })
            EditScheduleView(
                configManager,
                network,
                navController,
                userManager
            ).Content(modifier = Modifier)
        }

        slideComposable(SettingsScreen.EditPhase.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.edit_phase), {}, { navController.popBackStack() })
            EditPhaseView(navController, userManager, configManager, modifier = Modifier)
        }

        slideComposable(SettingsScreen.TemplateList.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.templates), {}, { navController.popBackStack() })
            ScheduleTemplateListView(configManager, templateStore, navController, userManager).Content(modifier = Modifier)
        }

        slideComposable(SettingsScreen.EditTemplate.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.edit_template), {}, { navController.popBackStack() })
            EditTemplateView(configManager, network, navController, userManager, templateStore).Content(modifier = Modifier)
        }

        slideComposable(SettingsScreen.APIKey.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.edit_api_key), {}, { navController.popBackStack() })
            ConfigureAPIKeyView(userManager.store, navController, configManager.themeStream, Modifier)
        }

        slideComposable(SettingsScreen.PowerStation.name) {
            configManager.powerStationDetail?.let { powerStationDetail ->
                topBarSettings.value = TopBarSettings(true, stringResource(R.string.settings_power_station), {}, { navController.popBackStack() })
                PowerStationSettingsView(configManager)
            }
        }

        slideComposable(SettingsScreen.DataSettings.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.settings_data), {}, { navController.popBackStack() })
            DataSettingsView(configManager, Modifier)
        }

        slideComposable(SettingsScreen.BatteryVersions.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.batteries), {}, { navController.popBackStack() })
            BatteryFirmwareVersionsView(configManager, network).Content(Modifier)
        }

        slideComposable(SettingsScreen.ConfigureExportLimit.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(id = R.string.export_limit), {}, { navController.popBackStack() })
            DeviceSettingItemView(configManager, network, DeviceSettingsItem.ExportLimit, navController).Content(Modifier)
        }

        slideComposable(SettingsScreen.ConfigureMaxSoc.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(id = R.string.max_soc), {}, { navController.popBackStack() })
            DeviceSettingItemView(configManager, network, DeviceSettingsItem.MaxSoc, navController).Content(Modifier)
        }

        slideComposable(SettingsScreen.ConfigurePeakShaving.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(id = R.string.peak_shaving), {}, { navController.popBackStack() })
            PeakShavingSettingsView(configManager, network, navController).Content(Modifier)
        }

        slideComposable(SettingsScreen.FactoryResetAppSettings.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.reset_app_settings), {}, { navController.popBackStack() })
            FactoryResetAppSettingsView(configManager, navController)
        }

        slideComposable(SettingsScreen.ConfigureWorkMode.name) {
            topBarSettings.value = TopBarSettings(true, stringResource(R.string.work_mode), {}, { navController.popBackStack() })
            WorkModeSettingsView(network, configManager, navController, userManager).Content()
        }

        debugGraph(topBarSettings, network)
    }
}

