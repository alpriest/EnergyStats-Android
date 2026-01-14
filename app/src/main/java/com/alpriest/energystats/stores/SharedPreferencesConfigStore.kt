package com.alpriest.energystats.stores

import android.content.SharedPreferences
import androidx.core.content.edit
import com.alpriest.energystats.shared.config.StoredConfig
import com.alpriest.energystats.shared.helpers.enumIntPreference
import com.alpriest.energystats.shared.helpers.jsonNullablePreference
import com.alpriest.energystats.shared.helpers.jsonPreference
import com.alpriest.energystats.shared.helpers.preference
import com.alpriest.energystats.shared.models.BatteryData
import com.alpriest.energystats.shared.models.BatteryTemperatureDisplayMode
import com.alpriest.energystats.shared.models.CT2DisplayMode
import com.alpriest.energystats.shared.models.ColorThemeMode
import com.alpriest.energystats.shared.models.DataCeiling
import com.alpriest.energystats.shared.models.DisplayUnit
import com.alpriest.energystats.shared.models.EarningsModel
import com.alpriest.energystats.shared.models.GenerationViewData
import com.alpriest.energystats.shared.models.ParameterGroup
import com.alpriest.energystats.shared.models.PowerFlowStringsSettings
import com.alpriest.energystats.shared.models.PowerStationDetail
import com.alpriest.energystats.shared.models.RefreshFrequency
import com.alpriest.energystats.shared.models.ScheduleTemplate
import com.alpriest.energystats.shared.models.SelfSufficiencyEstimateMode
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.alpriest.energystats.shared.models.SolcastSettings
import com.alpriest.energystats.shared.models.SummaryDateRange
import com.alpriest.energystats.shared.models.TotalYieldModel
import com.alpriest.energystats.shared.models.Variable
import com.alpriest.energystats.shared.models.WidgetTapAction
import com.alpriest.energystats.ui.summary.MonthYear
import com.alpriest.energystats.ui.summary.SummaryDateRangeSerialised
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class SharedPreferencesConfigStore(private val sharedPreferences: SharedPreferences) :
    StoredConfig {

    private enum class SharedPreferenceDeviceKey {
        IS_DEMO_USER,
        DEVICES,
        SELECTED_DEVICE_SN,
        POWER_STATION_DETAIL,
        BATTERY_CAPACITY_OVERRIDES
    }

    private enum class SharedPreferenceDisplayKey {
        USE_LARGE_DISPLAY,
        USE_COLOURED_FLOW_LINES,
        SHOW_BATTERY_TEMPERATURE,
        REFRESH_FREQUENCY,
        SHOW_SUNNY_BACKGROUND,
        DECIMAL_PLACES,
        SHOW_BATTERY_ESTIMATE,
        SHOW_USABLE_BATTERY_ONLY,
        SELF_SUFFICIENCY_ESTIMATE_MODE,
        SHOW_ESTIMATED_EARNINGS,
        DISPLAY_UNIT,
        SHOW_INVERTER_TEMPERATURES,
        SELECTED_PARAMETER_GRAPH_VARIABLES,
        SHOW_INVERTER_ICON,
        SHOW_HOME_TOTAL,
        SHOULD_INVERT_CT2,
        SHOW_GRID_TOTALS,
        SHOW_INVERTER_TYPE_NAME_ON_POWERFLOW,
        SHOW_INVERTER_PLANT_NAME_ON_POWERFLOW,
        SHOW_LAST_UPDATE_TIMESTAMP,
        SOLAR_RANGE_DEFINITIONS,
        PARAMETER_GROUPS,
        CURRENCY_SYMBOL,
        CURRENCY_CODE,
        FEED_IN_UNIT_PRICE,
        GRID_IMPORT_UNIT_PRICE,
        SHOULD_COMBINE_CT2_WITH_PVPOWER,
        SHOULD_COMBINE_CT2_WITH_LOADSPOWER,
        SHOW_GRAPH_VALUE_DESCRIPTIONS,
        COLOR_THEME_MODE,
        SOLCAST_SETTINGS,
        DATA_CEILING,
        TOTAL_YIELD_MODEL,
        SHOW_FINANCIAL_SUMMARY_ON_POWERFLOW,
        SEPARATE_PARAMETER_GRAPHS_BY_UNIT,
        VARIABLES,
        SHOW_BATTERY_SOC_AS_PERCENTAGE,
        USE_TRADITIONAL_LOAD_FORMULA,
        POWER_FLOW_STRINGS,
        SHOW_ESTIMATED_TIME_ON_WIDGET,
        SHOW_SELF_SUFFICIENCY_STATS_GRAPH_OVERLAY,
        TRUNCATED_Y_AXIS_ON_PARAMETER_GRAPHS,
        EARNINGS_MODEL,
        SUMMARY_DATE_RANGE,
        SCHEDULE_TEMPLATES,
        LAST_SOLCAST_REFRESH,
        WIDGET_TAP_ACTION,
        BATTERY_DATA,
        BATTERY_TEMPERATURE_DISPLAY_MODE,
        SHOW_INVERTER_SCHEDULE_QUICK_LINK,
        FETCH_SOLCAST_ON_APP_LAUNCH,
        CT2_DISPLAY_MODE,
        SHOW_STRING_TOTALS_AS_PERCENTAGE,
        GENERATION_VIEW_DATA,
        SHOW_INVERTER_CONSUMPTION,
        SHOW_BATTERY_SOC_ON_DAILY_STATS,
        INVERTER_WORK_MODES,
        ALLOW_NEGATIVE_LOADS
    }

    override fun clearDisplaySettings() {
        sharedPreferences.edit {
            SharedPreferenceDisplayKey.entries.forEach {
                remove(it.name)
            }
        }
    }

    override fun clearDeviceSettings() {
        sharedPreferences.edit {
            SharedPreferenceDeviceKey.entries.forEach {
                remove(it.name)
            }
        }
    }

    override var colorTheme: Int by preference(sharedPreferences, SharedPreferenceDisplayKey.COLOR_THEME_MODE.name, ColorThemeMode.Auto.value)
    override var showGraphValueDescriptions: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_GRAPH_VALUE_DESCRIPTIONS.name, true)
    override var shouldCombineCT2WithPVPower: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOULD_COMBINE_CT2_WITH_PVPOWER.name, true)
    override var shouldCombineCT2WithLoadsPower: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOULD_COMBINE_CT2_WITH_LOADSPOWER.name, true)
    override var gridImportUnitPrice: Double by preference(sharedPreferences, SharedPreferenceDisplayKey.GRID_IMPORT_UNIT_PRICE.name, 0.15)
    override var feedInUnitPrice: Double by preference(sharedPreferences, SharedPreferenceDisplayKey.FEED_IN_UNIT_PRICE.name, 0.05)
    override var currencyCode: String by preference(sharedPreferences, SharedPreferenceDisplayKey.CURRENCY_CODE.name, "GBP")
    override var currencySymbol: String by preference(sharedPreferences, SharedPreferenceDisplayKey.CURRENCY_SYMBOL.name, "£")
    override var showGridTotals: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_GRID_TOTALS.name, false)
    override var showHomeTotal: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_HOME_TOTAL.name, false)
    override var showInverterIcon: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_INVERTER_ICON.name, true)
    override var showUsableBatteryOnly: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_USABLE_BATTERY_ONLY.name, false)
    override var showBatteryEstimate: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_BATTERY_ESTIMATE.name, true)
    override var showSunnyBackground: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_SUNNY_BACKGROUND.name, true)
    override var decimalPlaces: Int by preference(sharedPreferences, SharedPreferenceDisplayKey.DECIMAL_PLACES.name, 2)
    override var selectedDeviceSN: String? by preference(sharedPreferences, SharedPreferenceDeviceKey.SELECTED_DEVICE_SN.name, null)
    override var devices: String? by preference(sharedPreferences, SharedPreferenceDeviceKey.DEVICES.name, null)
    override var showBatteryTemperature: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_BATTERY_TEMPERATURE.name, false)
    override var useColouredFlowLines: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.USE_COLOURED_FLOW_LINES.name, false)
    override var useLargeDisplay: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.USE_LARGE_DISPLAY.name, false)
    override var isDemoUser: Boolean by preference(sharedPreferences, SharedPreferenceDeviceKey.IS_DEMO_USER.name, false)
    override var showFinancialSummary: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_ESTIMATED_EARNINGS.name, false)
    override var displayUnit: Int by preference(sharedPreferences, SharedPreferenceDisplayKey.DISPLAY_UNIT.name, DisplayUnit.Adaptive.value)
    override var showInverterTemperatures: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_INVERTER_TEMPERATURES.name, false)
    override var shouldInvertCT2: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOULD_INVERT_CT2.name, false)
    override var showInverterTypeNameOnPowerflow: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_INVERTER_TYPE_NAME_ON_POWERFLOW.name, false)
    override var showInverterStationNameOnPowerflow: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_INVERTER_PLANT_NAME_ON_POWERFLOW.name, false)
    override var showLastUpdateTimestamp: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_LAST_UPDATE_TIMESTAMP.name, false)
    override var showFinancialSummaryOnFlowPage: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_FINANCIAL_SUMMARY_ON_POWERFLOW.name, false)
    override var useTraditionalLoadFormula: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.USE_TRADITIONAL_LOAD_FORMULA.name, false)
    override var separateParameterGraphsByUnit: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SEPARATE_PARAMETER_GRAPHS_BY_UNIT.name, true)
    override var showBatterySOCAsPercentage: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_BATTERY_SOC_AS_PERCENTAGE.name, false)
    override var showBatteryTimeEstimateOnWidget: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_ESTIMATED_TIME_ON_WIDGET.name, true)
    override var showSelfSufficiencyStatsGraphOverlay: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_SELF_SUFFICIENCY_STATS_GRAPH_OVERLAY.name, true)
    override var truncatedYAxisOnParameterGraphs: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.TRUNCATED_Y_AXIS_ON_PARAMETER_GRAPHS.name, false)
    override var showInverterScheduleQuickLink: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_INVERTER_SCHEDULE_QUICK_LINK.name, false)
    override var fetchSolcastOnAppLaunch: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.FETCH_SOLCAST_ON_APP_LAUNCH.name, false)
    override var showStringTotalsAsPercentage: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_STRING_TOTALS_AS_PERCENTAGE.name, false)
    override var showInverterConsumption: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_INVERTER_CONSUMPTION.name, false)
    override var showBatterySOCOnDailyStats: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.SHOW_BATTERY_SOC_ON_DAILY_STATS.name, false)
    override var allowNegativeLoad: Boolean by preference(sharedPreferences, SharedPreferenceDisplayKey.ALLOW_NEGATIVE_LOADS.name, false)
    override var ct2DisplayMode: Int by preference(sharedPreferences, SharedPreferenceDisplayKey.CT2_DISPLAY_MODE.name, CT2DisplayMode.Hidden.value)

    override var selfSufficiencyEstimateMode: SelfSufficiencyEstimateMode by enumIntPreference(
        sharedPreferences,
        SharedPreferenceDisplayKey.SELF_SUFFICIENCY_ESTIMATE_MODE.name,
        SelfSufficiencyEstimateMode.Off,
        readStorage = { SelfSufficiencyEstimateMode.fromInt(it) },
        writeStorage = { it.value }
    )
    override var refreshFrequency: RefreshFrequency by enumIntPreference(
        sharedPreferences,
        SharedPreferenceDisplayKey.REFRESH_FREQUENCY.name,
        RefreshFrequency.Auto,
        readStorage = { RefreshFrequency.fromInt(it) },
        writeStorage = { it.value }
    )
    override var dataCeiling: DataCeiling by enumIntPreference(
        sharedPreferences,
        SharedPreferenceDisplayKey.DATA_CEILING.name,
        DataCeiling.Mild,
        readStorage = { DataCeiling.fromInt(it) },
        writeStorage = { it.value }
    )
    override var totalYieldModel: TotalYieldModel by enumIntPreference(
        sharedPreferences,
        SharedPreferenceDisplayKey.TOTAL_YIELD_MODEL.name,
        TotalYieldModel.Off,
        readStorage = { TotalYieldModel.fromInt(it) },
        writeStorage = { it.value }
    )
    override var earningsModel: EarningsModel by enumIntPreference(
        sharedPreferences,
        SharedPreferenceDisplayKey.EARNINGS_MODEL.name,
        EarningsModel.Exported,
        readStorage = { EarningsModel.fromInt(it) },
        writeStorage = { it.value }
    )
    override var widgetTapAction: WidgetTapAction by enumIntPreference(
        sharedPreferences,
        SharedPreferenceDisplayKey.WIDGET_TAP_ACTION.name,
        WidgetTapAction.Launch,
        readStorage = { WidgetTapAction.fromInt(it) },
        writeStorage = { it.value }
    )
    override var batteryTemperatureDisplayMode: BatteryTemperatureDisplayMode by enumIntPreference(
        sharedPreferences,
        SharedPreferenceDisplayKey.BATTERY_TEMPERATURE_DISPLAY_MODE.name,
        BatteryTemperatureDisplayMode.Automatic,
        readStorage = { BatteryTemperatureDisplayMode.fromInt(it) },
        writeStorage = { it.value }
    )

    override var powerStationDetail: PowerStationDetail? by jsonNullablePreference(
        sharedPreferences,
        SharedPreferenceDeviceKey.POWER_STATION_DETAIL.name,
        object : TypeToken<PowerStationDetail?>() {})
    override var selectedParameterGraphVariables: List<String> by jsonPreference(
        sharedPreferences,
        SharedPreferenceDisplayKey.SELECTED_PARAMETER_GRAPH_VARIABLES.name,
        emptyList(),
        object : TypeToken<List<String>>() {})
    override var deviceBatteryOverrides: Map<String, String> by jsonPreference(
        sharedPreferences,
        SharedPreferenceDeviceKey.BATTERY_CAPACITY_OVERRIDES.name,
        emptyMap(),
        object : TypeToken<Map<String, String>>() {})
    override var solarRangeDefinitions: SolarRangeDefinitions by jsonPreference(
        sharedPreferences,
        SharedPreferenceDisplayKey.SOLAR_RANGE_DEFINITIONS.name,
        SolarRangeDefinitions.defaults,
        object : TypeToken<SolarRangeDefinitions>() {})
    override var parameterGroups: List<ParameterGroup> by jsonPreference(
        sharedPreferences,
        SharedPreferenceDisplayKey.PARAMETER_GROUPS.name,
        ParameterGroup.defaults,
        object : TypeToken<List<ParameterGroup>>() {})
    override var solcastSettings: SolcastSettings by jsonPreference(
        sharedPreferences,
        SharedPreferenceDisplayKey.SOLCAST_SETTINGS.name,
        SolcastSettings.defaults,
        object : TypeToken<SolcastSettings>() {})
    override var powerFlowStrings: PowerFlowStringsSettings by jsonPreference(
        sharedPreferences,
        SharedPreferenceDisplayKey.POWER_FLOW_STRINGS.name,
        PowerFlowStringsSettings.defaults,
        object : TypeToken<PowerFlowStringsSettings>() {})
    override var variables: List<Variable> by jsonPreference(sharedPreferences, SharedPreferenceDisplayKey.VARIABLES.name, listOf(), object : TypeToken<List<Variable>>() {})
    override var scheduleTemplates: List<ScheduleTemplate> by jsonPreference(
        sharedPreferences,
        SharedPreferenceDisplayKey.SCHEDULE_TEMPLATES.name,
        listOf(),
        object : TypeToken<List<ScheduleTemplate>>() {})
    override var batteryData: BatteryData? by jsonNullablePreference(sharedPreferences, SharedPreferenceDisplayKey.BATTERY_DATA.name, object : TypeToken<BatteryData>() {})
    override var generationViewData: GenerationViewData? by jsonNullablePreference(
        sharedPreferences,
        SharedPreferenceDisplayKey.GENERATION_VIEW_DATA.name,
        object : TypeToken<GenerationViewData>() {})
    override var workModes: List<String> by jsonPreference(sharedPreferences, SharedPreferenceDisplayKey.INVERTER_WORK_MODES.name, listOf(), object : TypeToken<List<String>>() {})

    override var summaryDateRange: SummaryDateRange
        get() {
            var data = sharedPreferences.getString(SharedPreferenceDisplayKey.SUMMARY_DATE_RANGE.name, null)
            if (data == null) {
                data = Gson().toJson(SummaryDateRangeSerialised(automatic = true, from = null, to = null))
                summaryDateRange = SummaryDateRange.Automatic
            }

            val deserialisedValue: SummaryDateRangeSerialised = Gson().fromJson(data, object : TypeToken<SummaryDateRangeSerialised>() {}.type)
            return if (deserialisedValue.automatic || deserialisedValue.from == null || deserialisedValue.to == null) {
                SummaryDateRange.Automatic
            } else {
                val from = deserialisedValue.from!!
                val to = deserialisedValue.to!!
                var fromLocalDate: LocalDate
                var toLocalDate: LocalDate

                if (from.year == 0 || from.month == 0) {
                    fromLocalDate = LocalDate.now()
                } else {
                    fromLocalDate = LocalDate.of(from.year, from.month, 1)
                }
                if (to.year == 0 || to.month == 0) {
                    toLocalDate = LocalDate.now()
                } else {
                    toLocalDate = LocalDate.of(to.year, to.month, 1)
                }

                SummaryDateRange.Manual(fromLocalDate, toLocalDate)
            }
        }
        set(value) {
            sharedPreferences.edit {
                val serialisedValue = when (value) {
                    is SummaryDateRange.Automatic -> SummaryDateRangeSerialised(automatic = true, from = null, to = null)
                    is SummaryDateRange.Manual -> SummaryDateRangeSerialised(
                        automatic = false, from = MonthYear(value.from.monthValue, value.from.year),
                        to = MonthYear(value.to.monthValue, value.to.year)
                    )
                }

                val jsonString = Gson().toJson(serialisedValue)
                putString(SharedPreferenceDisplayKey.SUMMARY_DATE_RANGE.name, jsonString)
            }
        }

    override var lastSolcastRefresh: LocalDateTime?
        get() {
            val epochMillis = sharedPreferences.getLong(SharedPreferenceDisplayKey.LAST_SOLCAST_REFRESH.name, 0)
            return if (epochMillis == 0L) {
                null
            } else {
                LocalDateTime.ofEpochSecond(epochMillis / 1000, (epochMillis % 1000 * 1_000_000).toInt(), ZoneOffset.UTC)
            }
        }
        set(value) {
            sharedPreferences.edit {
                if (value != null) {
                    val epochMillis = value.toInstant(ZoneOffset.UTC).toEpochMilli()
                    putLong(SharedPreferenceDisplayKey.LAST_SOLCAST_REFRESH.name, epochMillis)
                } else {
                    remove(SharedPreferenceDisplayKey.LAST_SOLCAST_REFRESH.name)
                }
            }
        }
}