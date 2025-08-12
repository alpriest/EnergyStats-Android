package com.alpriest.energystats.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alpriest.energystats.R
import com.alpriest.energystats.TopBarSettings
import com.alpriest.energystats.models.DeviceSettingsItem
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
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
import com.example.energystats.FactoryResetAppSettingsView

@Composable
fun NavigableSettingsView(
    topBarSettings: MutableState<TopBarSettings>,
    navController: NavHostController,
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

    NavHost(
        navController = navController,
        startDestination = SettingsScreen.Settings.name
    ) {
        composable(SettingsScreen.Settings.name) {
            topBarSettings.value = TopBarSettings(true, false, stringResource(R.string.settings_tab), {})
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
        composable(SettingsScreen.Battery.name) {
            topBarSettings.value = TopBarSettings(true, true, stringResource(R.string.battery), {})
            BatterySettingsView(
                navController = navController,
                config = configManager,
                modifier = Modifier
            )
        }
        composable(SettingsScreen.BatterySOC.name) {
            topBarSettings.value = TopBarSettings(true, true, stringResource(R.string.battery_soc), {})
            BatterySOCSettings(configManager = configManager, network = network, navController = navController, userManager = userManager).Content(modifier = Modifier)
        }
        composable(SettingsScreen.BatteryChargeTimes.name) {
            topBarSettings.value = TopBarSettings(true, true, stringResource(R.string.battery_charge_schedule), {})
            BatteryChargeScheduleSettingsView(configManager = configManager, network = network, navController = navController, userManager = userManager).Content(modifier = Modifier)
        }
        composable(SettingsScreen.Inverter.name) {
            topBarSettings.value = TopBarSettings(true, true, stringResource(R.string.inverter), {})
            InverterSettingsView(configManager = configManager, network = network, navController = navController, modifier = Modifier)
        }
        composable(SettingsScreen.InverterSchedule.name) {
            topBarSettings.value = TopBarSettings(true, true, stringResource(R.string.manage_schedules), {})
            ScheduleSummaryView(configManager, network, navController, userManager, templateStore).Content(modifier = Modifier)
        }
        composable(SettingsScreen.Dataloggers.name) {
            topBarSettings.value = TopBarSettings(true, true, stringResource(R.string.dataloggers), {})
            DataLoggerViewContainer(network = network, configManager = configManager, navController = navController).Content(modifier = Modifier)
        }
        composable(SettingsScreen.FinancialModel.name) {
            topBarSettings.value = TopBarSettings(true, true, stringResource(R.string.financial_model), {})
            SettingsPage(Modifier) {
                FinancialsSettingsView(configManager)
            }
        }
        composable(SettingsScreen.SelfSufficiencyEstimates.name) {
            topBarSettings.value = TopBarSettings(true, true, stringResource(R.string.self_sufficiency_estimates), {})
            SettingsPage(Modifier) {
                SelfSufficiencySettingsView(configManager)
            }
        }
        composable(SettingsScreen.SolarBandings.name) {
            topBarSettings.value = TopBarSettings(true, true, stringResource(R.string.sun_display_thresholds), {})
            SolarBandingSettingsView(navController, configManager, Modifier)
        }
        composable(SettingsScreen.FAQ.name) {
            topBarSettings.value = TopBarSettings(true, true, stringResource(R.string.frequently_asked_questions), {})
            FAQView()
        }
        composable(SettingsScreen.SolcastSolarPrediction.name) {
            topBarSettings.value = TopBarSettings(true, true, stringResource(R.string.solcast_solar_prediction), {})
            SolcastSettingsView(navController, configManager, userManager, solarForecastingProvider).Content(modifier = Modifier)
        }
        composable(SettingsScreen.EditSchedule.name) {
            topBarSettings.value = TopBarSettings(true, true,  stringResource(R.string.edit_schedule), {})
            EditScheduleView(
                configManager,
                network,
                navController,
                userManager
            ).Content(modifier = Modifier)
        }

        composable(SettingsScreen.EditPhase.name) {
            topBarSettings.value = TopBarSettings(true, true,  stringResource(R.string.edit_phase), {})
            EditPhaseView(navController, userManager, configManager, modifier = Modifier)
        }

        composable(SettingsScreen.TemplateList.name) {
            topBarSettings.value = TopBarSettings(true, true,  stringResource(R.string.templates), {})
            ScheduleTemplateListView(configManager, templateStore, navController, userManager).Content(modifier = Modifier)
        }

        composable(SettingsScreen.EditTemplate.name) {
            topBarSettings.value = TopBarSettings(true, true, stringResource(R.string.edit_template), {})
            EditTemplateView(configManager, network, navController, userManager, templateStore).Content(modifier = Modifier)
        }

        composable(SettingsScreen.APIKey.name) {
            topBarSettings.value = TopBarSettings(true, true,  stringResource(R.string.edit_api_key), {})
            ConfigureAPIKeyView(userManager.store, navController, configManager.themeStream, Modifier)
        }

        composable(SettingsScreen.PowerStation.name) {
            configManager.powerStationDetail?.let { powerStationDetail ->
                topBarSettings.value = TopBarSettings(true, true,  stringResource(R.string.settings_power_station), {})
                PowerStationSettingsView(powerStationDetail)
            }
        }

        composable(SettingsScreen.DataSettings.name) {
            topBarSettings.value = TopBarSettings(true, true,  stringResource(R.string.settings_data), {})
            DataSettingsView(configManager, Modifier)
        }

        composable(SettingsScreen.BatteryVersions.name) {
            topBarSettings.value = TopBarSettings(true, true,  stringResource(R.string.batteries), {})
            BatteryFirmwareVersionsView(configManager, network).Content(Modifier)
        }

        composable(SettingsScreen.ConfigureExportLimit.name) {
            topBarSettings.value = TopBarSettings(true, true,  stringResource(id = R.string.export_limit), {})
            DeviceSettingItemView(configManager, network, DeviceSettingsItem.ExportLimit, navController).Content(Modifier)
        }

        composable(SettingsScreen.ConfigureMaxSoc.name) {
            topBarSettings.value = TopBarSettings(true, true,  stringResource(id = R.string.max_soc), {})
            DeviceSettingItemView(configManager, network, DeviceSettingsItem.MaxSoc, navController).Content(Modifier)
        }

        composable(SettingsScreen.ConfigurePeakShaving.name) {
            topBarSettings.value = TopBarSettings(true, true,  stringResource(id = R.string.peak_shaving), {})
            PeakShavingSettingsView(configManager, network, navController).Content(Modifier)
        }

        composable(SettingsScreen.FactoryResetAppSettings.name) {
            topBarSettings.value = TopBarSettings(true, true, stringResource(R.string.reset_app_settings), {})
            FactoryResetAppSettingsView(configManager, navController)
        }

        composable(SettingsScreen.ConfigureWorkMode.name) {
            topBarSettings.value = TopBarSettings(true, true, stringResource(R.string.work_mode), {})
            WorkModeSettingsView(network, configManager, navController, userManager).Content()
        }

        debugGraph(topBarSettings, network)
    }
}

