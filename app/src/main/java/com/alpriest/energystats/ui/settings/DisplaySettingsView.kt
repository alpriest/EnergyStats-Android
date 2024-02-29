package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.R
import com.alpriest.energystats.models.kW
import com.alpriest.energystats.models.w
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.SegmentedControl
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun DisplaySettingsView(config: ConfigManaging, modifier: Modifier = Modifier) {
    val largeDisplayState = rememberSaveable { mutableStateOf(config.useLargeDisplay) }
    val colouredFlowLinesState = rememberSaveable { mutableStateOf(config.useColouredFlowLines) }
    val showSunnyBackgroundState = rememberSaveable { mutableStateOf(config.showSunnyBackground) }
    val decimalPlacesState = rememberSaveable { mutableIntStateOf(config.decimalPlaces) }
    val totalYieldModelState = rememberSaveable { mutableStateOf(config.totalYieldModel) }
    val displayUnitState = rememberSaveable { mutableStateOf(config.displayUnit) }
    val showHomeTotalState = rememberSaveable { mutableStateOf(config.showHomeTotal) }
    val showGridTotalsState = rememberSaveable { mutableStateOf(config.showGridTotals) }
    val showLastUpdateTimestampState = rememberSaveable { mutableStateOf(config.showLastUpdateTimestamp) }
    val showGraphValueDescriptionsState = rememberSaveable { mutableStateOf(config.showGraphValueDescriptions) }
    val separateParameterGraphsByUnitState = rememberSaveable { mutableStateOf(config.separateParameterGraphsByUnit) }
    val colorThemeModeState = rememberSaveable { mutableStateOf(config.colorThemeMode) }
    val dataCeilingState = rememberSaveable { mutableStateOf(config.dataCeiling) }
    val useExperimentalLoadFormulaState = rememberSaveable { mutableStateOf(config.useExperimentalLoadFormula) }
    val showBatteryAsPercentageState = rememberSaveable { mutableStateOf(config.showBatteryAsPercentage) }
    val context = LocalContext.current

    SettingsColumnWithChild(
        modifier = modifier
    ) {
        SettingsTitleView(stringResource(R.string.display))

        SettingsCheckbox(
            title = stringResource(R.string.increase_sizes_for_large_display),
            state = largeDisplayState,
            onUpdate = { config.useLargeDisplay = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_coloured_flow_lines),
            state = colouredFlowLinesState,
            onUpdate = { config.useColouredFlowLines = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_home_usage_total),
            state = showHomeTotalState,
            onUpdate = { config.showHomeTotal = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_daily_grid_totals),
            state = showGridTotalsState,
            onUpdate = { config.showGridTotals = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_sunny_background),
            state = showSunnyBackgroundState,
            onUpdate = { config.showSunnyBackground = it }
        )

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

        SettingsCheckbox(
            title = stringResource(R.string.show_last_update_timestamp),
            state = showLastUpdateTimestampState,
            onUpdate = { config.showLastUpdateTimestamp = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_graph_value_descriptions),
            state = showGraphValueDescriptionsState,
            onUpdate = { config.showGraphValueDescriptions = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.separate_parameter_graphs_by_unit),
            state = separateParameterGraphsByUnitState,
            onUpdate = { config.separateParameterGraphsByUnit = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_battery_percentage_remaining),
            state = showBatteryAsPercentageState,
            onUpdate = { config.showBatteryAsPercentage = it }
        )
    }

    SolarStringsSettingsView(config, modifier)

    SettingsColumnWithChild(
        modifier = modifier
    ) {
        SettingsSegmentedControl(
            title = stringResource(R.string.solar),
            segmentedControl = {
                val items = listOf(
                    TotalYieldModel.Off,
                    TotalYieldModel.EnergyStats
                )
                SegmentedControl(
                    items = items.map { it.title(context) },
                    defaultSelectedItemIndex = items.indexOf(totalYieldModelState.value),
                    color = colors.primary
                ) {
                    totalYieldModelState.value = items[it]
                    config.totalYieldModel = items[it]
                }
            },
            footer = when (totalYieldModelState.value) {
                TotalYieldModel.Off -> null
                TotalYieldModel.EnergyStats -> buildAnnotatedString {
                    append(stringResource(R.string.energystats_total_yield_description))
                }
            }
        )
    }

    SettingsColumnWithChild(
        modifier = modifier
    ) {
        SettingsSegmentedControl(
            title = stringResource(R.string.units),
            segmentedControl = {
                val items = listOf(
                    DisplayUnit.Watts,
                    DisplayUnit.Kilowatts,
                    DisplayUnit.Adaptive
                )
                SegmentedControl(
                    items = items.map { it.title(context) },
                    defaultSelectedItemIndex = items.indexOf(displayUnitState.value),
                    color = colors.primary
                ) {
                    displayUnitState.value = items[it]
                    config.displayUnit = items[it]
                }
            },
            footer = buildAnnotatedString {
                when (displayUnitState.value) {
                    DisplayUnit.Kilowatts -> append(
                        stringResource(
                            R.string.display_unit_kilowatts_description,
                            3.456.kW(decimalPlacesState.value),
                            0.123.kW(decimalPlacesState.value)
                        )
                    )

                    DisplayUnit.Watts -> append(stringResource(R.string.display_unit_watts_description, 3.456.w(), 0.123.w()))
                    DisplayUnit.Adaptive -> append(stringResource(R.string.display_unit_adaptive_description, 3.456.kW(decimalPlacesState.value), 0.123.w()))
                }
            }
        )
    }

    SettingsColumnWithChild(
        modifier = modifier
    ) {
        SettingsSegmentedControl(
            title = stringResource(R.string.data_ceiling),
            segmentedControl = {
                val items = listOf(DataCeiling.None, DataCeiling.Mild, DataCeiling.Enhanced)
                SegmentedControl(
                    items = items.map { it.title(context) },
                    defaultSelectedItemIndex = items.indexOf(dataCeilingState.value),
                    color = colors.primary
                ) {
                    dataCeilingState.value = items[it]
                    config.dataCeiling = items[it]
                }
            },
            footer = buildAnnotatedString {
                when (dataCeilingState.value) {
                    DataCeiling.None -> append(stringResource(R.string.data_ceiling_none_description))
                    DataCeiling.Mild -> append(stringResource(R.string.data_ceiling_mild_description))
                    DataCeiling.Enhanced -> append(stringResource(R.string.data_ceiling_enhanced_description))
                }
            }
        )
    }

    SettingsColumnWithChild(
        modifier = modifier
    ) {
        SettingsCheckbox(
            title = "Use experimental load formula",
            state = useExperimentalLoadFormulaState,
            onUpdate = { config.useExperimentalLoadFormula = it },
            footer = buildAnnotatedString {
                append("Uses a formula to calculate load which should handle +ve/-ve CT2 better in house load. Changes only take effect on next data fetch.")
            }
        )
    }
}

@Preview(showBackground = true, heightDp = 940)
@Composable
fun DisplaySettingsViewPreview() {
    EnergyStatsTheme(colorThemeMode = ColorThemeMode.Light) {
        SettingsPage {
            DisplaySettingsView(
                config = FakeConfigManager(),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}