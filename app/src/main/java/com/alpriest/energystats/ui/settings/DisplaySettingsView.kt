package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.models.ColorThemeMode
import com.alpriest.energystats.shared.models.TotalYieldModel
import com.alpriest.energystats.shared.models.WidgetTapAction
import com.alpriest.energystats.ui.helpers.SegmentedControl
import com.alpriest.energystats.ui.settings.financial.MakeCurrencySymbolField
import com.alpriest.energystats.ui.settings.solar.SolarStringsSettingsView
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun DisplaySettingsView(configManager: ConfigManaging, modifier: Modifier = Modifier, navController: NavHostController) {
    val showBatteryTimeEstimateOnWidgetState = rememberSaveable { mutableStateOf(configManager.showBatteryTimeEstimateOnWidget) }
    val largeDisplayState = rememberSaveable { mutableStateOf(configManager.useLargeDisplay) }
    val colouredFlowLinesState = rememberSaveable { mutableStateOf(configManager.useColouredFlowLines) }
    val showSunnyBackgroundState = rememberSaveable { mutableStateOf(configManager.showSunnyBackground) }
    val decimalPlacesState = rememberSaveable { mutableIntStateOf(configManager.decimalPlaces) }
    val totalYieldModelState = rememberSaveable { mutableStateOf(configManager.totalYieldModel == TotalYieldModel.EnergyStats) }
    val showHomeTotalState = rememberSaveable { mutableStateOf(configManager.showHomeTotal) }
    val showGridTotalsState = rememberSaveable { mutableStateOf(configManager.showGridTotals) }
    val showLastUpdateTimestampState = rememberSaveable { mutableStateOf(configManager.showLastUpdateTimestamp) }
    val showGraphValueDescriptionsState = rememberSaveable { mutableStateOf(configManager.showGraphValueDescriptions) }
    val separateParameterGraphsByUnitState = rememberSaveable { mutableStateOf(configManager.separateParameterGraphsByUnit) }
    val colorThemeModeState = rememberSaveable { mutableStateOf(configManager.colorThemeMode) }
    val showBatteryAsPercentageState = rememberSaveable { mutableStateOf(configManager.showBatteryAsPercentage) }
    val widgetTapActionState = rememberSaveable { mutableStateOf(configManager.widgetTapAction) }
    val currencySymbol = rememberSaveable { mutableStateOf(configManager.currencySymbol) }
    val context = LocalContext.current

    SettingsColumn(
        modifier = modifier,
        header = stringResource(R.string.display),
        footer = stringResource(R.string.some_settings_will_only_take_effect_on_the_next_data_refresh)
    ) {
        SettingsCheckbox(
            title = stringResource(R.string.increase_sizes_for_large_display),
            state = largeDisplayState,
            onUpdate = { configManager.useLargeDisplay = it }
        )
        HorizontalDivider()

        SettingsCheckbox(
            title = stringResource(R.string.show_coloured_flow_lines),
            state = colouredFlowLinesState,
            onUpdate = { configManager.useColouredFlowLines = it }
        )
        HorizontalDivider()

        SettingsCheckbox(
            title = stringResource(R.string.show_home_usage_total),
            state = showHomeTotalState,
            onUpdate = { configManager.showHomeTotal = it }
        )
        HorizontalDivider()

        SettingsCheckbox(
            title = stringResource(R.string.show_daily_grid_totals),
            state = showGridTotalsState,
            onUpdate = { configManager.showGridTotals = it }
        )
        HorizontalDivider()

        SettingsCheckbox(
            title = stringResource(R.string.show_sunny_background),
            state = showSunnyBackgroundState,
            onUpdate = { configManager.showSunnyBackground = it }
        )
        HorizontalDivider()

        SettingsSegmentedControl(
            title = stringResource(R.string.appearance),
            segmentedControl = {
                val items = listOf(
                    ColorThemeMode.Light,
                    ColorThemeMode.Dark,
                    ColorThemeMode.Auto
                )
                SegmentedControl(
                    items = items.map { it.title(context) },
                    defaultSelectedItemIndex = items.indexOf(colorThemeModeState.value),
                    color = colorScheme.primary
                ) {
                    colorThemeModeState.value = items[it]
                    configManager.colorThemeMode = items[it]
                }
            }
        )
        HorizontalDivider()

        SettingsSegmentedControl(
            title = stringResource(R.string.decimal_places),
            segmentedControl = {
                val items = listOf("2", "3")
                SegmentedControl(
                    items = items,
                    defaultSelectedItemIndex = items.indexOf(decimalPlacesState.intValue.toString()),
                    color = colorScheme.primary
                ) {
                    decimalPlacesState.intValue = items[it].toInt()
                    configManager.decimalPlaces = items[it].toInt()
                }
            }
        )
        HorizontalDivider()

        SettingsCheckbox(
            title = stringResource(R.string.show_last_update_timestamp),
            state = showLastUpdateTimestampState,
            onUpdate = { configManager.showLastUpdateTimestamp = it }
        )
        HorizontalDivider()

        SettingsCheckbox(
            title = stringResource(R.string.show_graph_value_descriptions),
            state = showGraphValueDescriptionsState,
            onUpdate = { configManager.showGraphValueDescriptions = it }
        )
        HorizontalDivider()

        SettingsCheckbox(
            title = stringResource(R.string.separate_parameter_graphs_by_unit),
            state = separateParameterGraphsByUnitState,
            onUpdate = { configManager.separateParameterGraphsByUnit = it }
        )
        HorizontalDivider()

        SettingsCheckbox(
            title = stringResource(R.string.show_battery_percentage_remaining),
            state = showBatteryAsPercentageState,
            onUpdate = { configManager.showBatteryAsPercentage = it }
        )
        HorizontalDivider()

        SettingsCheckbox(
            title = stringResource(R.string.show_solar_yield),
            state = totalYieldModelState,
            infoText = stringResource(R.string.solar_yield_description),
            onUpdate = { configManager.totalYieldModel = if (it) TotalYieldModel.EnergyStats else TotalYieldModel.Off }
        )
        HorizontalDivider()

        SolarStringsSettingsView(configManager)
        HorizontalDivider()

        InlineSettingsNavButton(stringResource(R.string.sun_display_thresholds)) { navController.navigate(SettingsScreen.SolarBandings.name) }
        HorizontalDivider()

        SettingsCheckbox(
            title = stringResource(R.string.show_battery_estimate_on_widget),
            state = showBatteryTimeEstimateOnWidgetState,
            onUpdate = { configManager.showBatteryTimeEstimateOnWidget = it }
        )
        HorizontalDivider()

        SettingsSegmentedControl(
            title = stringResource(R.string.widget_tap_action),
            segmentedControl = {
                val items = listOf(WidgetTapAction.Launch, WidgetTapAction.Refresh)
                SegmentedControl(
                    items = items.map { it.title(context) },
                    defaultSelectedItemIndex = items.indexOf(widgetTapActionState.value),
                    color = colorScheme.primary
                ) {
                    widgetTapActionState.value = items[it]
                    configManager.widgetTapAction = items[it]
                }
            }
        )

        HorizontalDivider()

        MakeCurrencySymbolField(configManager, currencySymbol)
    }
}

@Preview(showBackground = true, heightDp = 940)
@Composable
fun DisplaySettingsViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        SettingsPage(Modifier) {
            DisplaySettingsView(
                configManager = FakeConfigManager(),
                modifier = Modifier.padding(horizontal = SettingsPadding.PANEL_OUTER_HORIZONTAL),
                navController = NavHostController(context = LocalContext.current)
            )
        }
    }
}
