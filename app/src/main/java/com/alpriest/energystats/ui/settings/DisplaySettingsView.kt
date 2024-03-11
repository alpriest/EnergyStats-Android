package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.SegmentedControl
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun DisplaySettingsView(config: ConfigManaging, modifier: Modifier = Modifier, navController: NavHostController) {
    val largeDisplayState = rememberSaveable { mutableStateOf(config.useLargeDisplay) }
    val colouredFlowLinesState = rememberSaveable { mutableStateOf(config.useColouredFlowLines) }
    val showSunnyBackgroundState = rememberSaveable { mutableStateOf(config.showSunnyBackground) }
    val decimalPlacesState = rememberSaveable { mutableIntStateOf(config.decimalPlaces) }
    val totalYieldModelState = rememberSaveable { mutableStateOf(config.totalYieldModel == TotalYieldModel.Off) }
    val showHomeTotalState = rememberSaveable { mutableStateOf(config.showHomeTotal) }
    val showGridTotalsState = rememberSaveable { mutableStateOf(config.showGridTotals) }
    val showLastUpdateTimestampState = rememberSaveable { mutableStateOf(config.showLastUpdateTimestamp) }
    val showGraphValueDescriptionsState = rememberSaveable { mutableStateOf(config.showGraphValueDescriptions) }
    val separateParameterGraphsByUnitState = rememberSaveable { mutableStateOf(config.separateParameterGraphsByUnit) }
    val colorThemeModeState = rememberSaveable { mutableStateOf(config.colorThemeMode) }
    val showBatteryAsPercentageState = rememberSaveable { mutableStateOf(config.showBatteryAsPercentage) }
    val context = LocalContext.current

    SettingsColumn(
        modifier = modifier,
        header = stringResource(R.string.display)
    ) {
        SettingsCheckbox(
            title = stringResource(R.string.increase_sizes_for_large_display),
            state = largeDisplayState,
            onUpdate = { config.useLargeDisplay = it }
        )
        Divider()

        SettingsCheckbox(
            title = stringResource(R.string.show_coloured_flow_lines),
            state = colouredFlowLinesState,
            onUpdate = { config.useColouredFlowLines = it }
        )
        Divider()

        SettingsCheckbox(
            title = stringResource(R.string.show_home_usage_total),
            state = showHomeTotalState,
            onUpdate = { config.showHomeTotal = it }
        )
        Divider()

        SettingsCheckbox(
            title = stringResource(R.string.show_daily_grid_totals),
            state = showGridTotalsState,
            onUpdate = { config.showGridTotals = it }
        )
        Divider()

        SettingsCheckbox(
            title = stringResource(R.string.show_sunny_background),
            state = showSunnyBackgroundState,
            onUpdate = { config.showSunnyBackground = it }
        )
        Divider()

        SettingsSegmentedControl(
            title = "Appearance",
            segmentedControl = {
                val items = listOf(
                    ColorThemeMode.Light,
                    ColorThemeMode.Dark,
                    ColorThemeMode.Auto
                )
                SegmentedControl(
                    items = items.map { it.title(context) },
                    defaultSelectedItemIndex = items.indexOf(colorThemeModeState.value),
                    color = colors.primary
                ) {
                    colorThemeModeState.value = items[it]
                    config.colorThemeMode = items[it]
                }
            }
        )
        Divider()

        SettingsSegmentedControl(
            title = stringResource(R.string.decimal_places),
            segmentedControl = {
                val items = listOf("2", "3")
                SegmentedControl(
                    items = items,
                    defaultSelectedItemIndex = items.indexOf(decimalPlacesState.value.toString()),
                    color = colors.primary
                ) {
                    decimalPlacesState.value = items[it].toInt()
                    config.decimalPlaces = items[it].toInt()
                }
            }
        )
        Divider()

        SettingsCheckbox(
            title = stringResource(R.string.show_last_update_timestamp),
            state = showLastUpdateTimestampState,
            onUpdate = { config.showLastUpdateTimestamp = it }
        )
        Divider()

        SettingsCheckbox(
            title = stringResource(R.string.show_graph_value_descriptions),
            state = showGraphValueDescriptionsState,
            onUpdate = { config.showGraphValueDescriptions = it }
        )
        Divider()

        SettingsCheckbox(
            title = stringResource(R.string.separate_parameter_graphs_by_unit),
            state = separateParameterGraphsByUnitState,
            onUpdate = { config.separateParameterGraphsByUnit = it }
        )
        Divider()

        SettingsCheckbox(
            title = stringResource(R.string.show_battery_percentage_remaining),
            state = showBatteryAsPercentageState,
            onUpdate = { config.showBatteryAsPercentage = it }
        )
        Divider()

        SettingsCheckbox(
            title = stringResource(R.string.solar),
            infoText = stringResource(R.string.energystats_total_yield_description),
            state = totalYieldModelState,
            onUpdate = {  config.totalYieldModel = if (it) TotalYieldModel.EnergyStats else TotalYieldModel.Off }
        )
        Divider()

        InlineSettingsNavButton(stringResource(R.string.sun_display_variation_thresholds)) { navController.navigate(SettingsScreen.SolarBandings.name) }
    }

    SolarStringsSettingsView(config, modifier)
}

@Preview(showBackground = true, heightDp = 940)
@Composable
fun DisplaySettingsViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        SettingsPage {
            DisplaySettingsView(
                config = FakeConfigManager(),
                modifier = Modifier.padding(horizontal = 12.dp),
                navController = NavHostController(context = LocalContext.current)
            )
        }
    }
}