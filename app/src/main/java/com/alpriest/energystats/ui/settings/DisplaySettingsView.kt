package com.alpriest.energystats.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.alpriest.energystats.R
import com.alpriest.energystats.models.kW
import com.alpriest.energystats.models.w
import com.alpriest.energystats.preview.FakeConfigManager
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.SegmentedControl
import com.alpriest.energystats.ui.theme.EnergyStatsTheme

@Composable
fun DisplaySettingsView(config: ConfigManaging, navController: NavHostController, modifier: Modifier = Modifier) {
    val largeDisplayState = rememberSaveable { mutableStateOf(config.useLargeDisplay) }
    val colouredFlowLinesState = rememberSaveable { mutableStateOf(config.useColouredFlowLines) }
    val showSunnyBackgroundState = rememberSaveable { mutableStateOf(config.showSunnyBackground) }
    val decimalPlacesState = rememberSaveable { mutableStateOf(config.decimalPlaces) }
    val showTotalYieldState = rememberSaveable { mutableStateOf(config.showTotalYield) }
    val showEstimatedEarningsState = rememberSaveable { mutableStateOf(config.showEstimatedEarnings) }
    val displayUnitState = rememberSaveable { mutableStateOf(config.displayUnit) }
    val showHomeTotalState = rememberSaveable { mutableStateOf(config.showHomeTotal) }
    val showGridTotalsState = rememberSaveable { mutableStateOf(config.showGridTotals) }
    val showLastUpdateTimestampState = rememberSaveable { mutableStateOf(config.showLastUpdateTimestamp) }
    val context = LocalContext.current

    SettingsColumnWithChild(
        modifier = modifier
    ) {
        SettingsTitleView(stringResource(R.string.display))

        SettingsCheckbox(
            title = stringResource(R.string.increase_sizes_for_large_display),
            state = largeDisplayState,
            onConfigUpdate = { config.useLargeDisplay = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_coloured_flow_lines),
            state = colouredFlowLinesState,
            onConfigUpdate = { config.useColouredFlowLines = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_total_yield),
            state = showTotalYieldState,
            onConfigUpdate = { config.showTotalYield = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_home_usage_total),
            state = showHomeTotalState,
            onConfigUpdate = { config.showHomeTotal = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_daily_grid_totals),
            state = showGridTotalsState,
            onConfigUpdate = { config.showGridTotals = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_sunny_background),
            state = showSunnyBackgroundState,
            onConfigUpdate = { config.showSunnyBackground = it }
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
                        ))
                    DisplayUnit.Watts -> append(stringResource(R.string.display_unit_watts_description, 3.456.w(), 0.123.w()))
                    DisplayUnit.Adaptive -> append(stringResource(R.string.display_unit_adaptive_description, 3.456.kW(decimalPlacesState.value), 0.123.w()))
                }
            }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_last_update_timestamp),
            state = showLastUpdateTimestampState,
            onConfigUpdate = { config.showLastUpdateTimestamp = it }
        )

        SettingsCheckbox(
            title = stringResource(R.string.show_estimated_earnings),
            state = showEstimatedEarningsState,
            onConfigUpdate = { config.showEstimatedEarnings = it },
            footer = buildAnnotatedString {
                append(stringResource(R.string.shows_earnings_today_this_month_this_year_and_all_time_based_on_a_crude_calculation_of))
                append(" ")
                withStyle(
                    style = SpanStyle(fontStyle = FontStyle.Italic, color = colors.onSecondary)
                ) {
                    append(stringResource(R.string.feed_in_kwh_price_per_kwh))
                }
                append(" ")
                append(stringResource(R.string.as_configured_on_foxess_cloud))
            }
        )

        SettingsNavButton(stringResource(R.string.approximations)) { navController.navigate(SettingsScreen.Approximations.name) }
        SettingsNavButton(stringResource(R.string.sun_display_variation_thresholds)) { navController.navigate(SettingsScreen.SolarBandings.name) }
    }
}

@Preview(showBackground = true, heightDp = 640)
@Composable
fun DisplaySettingsViewPreview() {
    EnergyStatsTheme(darkTheme = false) {
        DisplaySettingsView(config = FakeConfigManager(), navController = NavHostController(LocalContext.current), modifier = Modifier.padding(horizontal = 12.dp))
    }
}