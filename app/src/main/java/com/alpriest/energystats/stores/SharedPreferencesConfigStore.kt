package com.alpriest.energystats.stores

import android.content.SharedPreferences
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.ConfigInterface
import com.alpriest.energystats.models.PowerStationDetail
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGroup
import com.alpriest.energystats.ui.settings.BatteryTemperatureDisplayMode
import com.alpriest.energystats.ui.settings.DataCeiling
import com.alpriest.energystats.ui.settings.PowerFlowStringsSettings
import com.alpriest.energystats.ui.settings.financial.EarningsModel
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.alpriest.energystats.ui.settings.solcast.SolcastSettings
import com.alpriest.energystats.ui.summary.SummaryDateRange
import com.alpriest.energystats.ui.summary.SummaryDateRangeSerialised
import com.alpriest.energystats.ui.theme.SolarRangeDefinitions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.ZoneOffset

class SharedPreferencesConfigStore(private val sharedPreferences: SharedPreferences) :
    ConfigInterface {

    private enum class SharedPreferenceDeviceKey {
        IS_DEMO_USER,
        DEVICES,
        SELECTED_DEVICE_SN,
        POWER_STATION_DETAIL,
        BATTERY_CAPACITY_OVERRIDES,
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
        EARNINGS_MODEl,
        SUMMARY_DATE_RANGE,
        SCHEDULE_TEMPLATES,
        LAST_SOLCAST_REFRESH,
        WIDGET_TAP_ACTION,
        BATTERY_VIEW_MODEL,
        BATTERY_TEMPERATURE_DISPLAY_MODE,
        SHOW_INVERTER_SCHEDULE_QUICK_LINK,
        FETCH_SOLCAST_ON_APP_LAUNCH,
        SHOW_CT2_VALUE_AS_STRING;
    }

    override fun clearDisplaySettings() {
        val editor = sharedPreferences.edit()

        SharedPreferenceDisplayKey.entries.forEach {
            editor.remove(it.name)
        }

        editor.apply()
    }

    override fun clearDeviceSettings() {
        val editor = sharedPreferences.edit()

        SharedPreferenceDeviceKey.entries.forEach {
            editor.remove(it.name)
        }

        editor.apply()
    }

    override var colorTheme: Int
        get() = sharedPreferences.getInt(SharedPreferenceDisplayKey.COLOR_THEME_MODE.name, 0)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(SharedPreferenceDisplayKey.COLOR_THEME_MODE.name, value)
            editor.apply()
        }

    override var showGraphValueDescriptions: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_GRAPH_VALUE_DESCRIPTIONS.name, true)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_GRAPH_VALUE_DESCRIPTIONS.name, value)
            editor.apply()
        }

    override var shouldCombineCT2WithPVPower: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOULD_COMBINE_CT2_WITH_PVPOWER.name, true)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOULD_COMBINE_CT2_WITH_PVPOWER.name, value)
            editor.apply()
        }

    override var powerStationDetail: PowerStationDetail?
        get() {
            val data: String? = sharedPreferences.getString(SharedPreferenceDeviceKey.POWER_STATION_DETAIL.name, null) ?: return null

            return Gson().fromJson(data, object : TypeToken<PowerStationDetail>() {}.type)
        }
        set(value) {
            val editor = sharedPreferences.edit()
            val jsonString = Gson().toJson(value)
            editor.putString(SharedPreferenceDeviceKey.POWER_STATION_DETAIL.name, jsonString)
            editor.apply()
        }

    override var shouldCombineCT2WithLoadsPower: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOULD_COMBINE_CT2_WITH_LOADSPOWER.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOULD_COMBINE_CT2_WITH_LOADSPOWER.name, value)
            editor.apply()
        }

    override var gridImportUnitPrice: Double
        get() = (sharedPreferences.getString(SharedPreferenceDisplayKey.GRID_IMPORT_UNIT_PRICE.name, "0.15") ?: "0.15").toDouble()
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(SharedPreferenceDisplayKey.GRID_IMPORT_UNIT_PRICE.name, value.toString())
            editor.apply()
        }

    override var feedInUnitPrice: Double
        get() = (sharedPreferences.getString(SharedPreferenceDisplayKey.FEED_IN_UNIT_PRICE.name, "0.05") ?: "0.05").toDouble()
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(SharedPreferenceDisplayKey.FEED_IN_UNIT_PRICE.name, value.toString())
            editor.apply()
        }

    override var currencyCode: String
        get() = sharedPreferences.getString(SharedPreferenceDisplayKey.CURRENCY_CODE.name, "GBP") ?: "GBP"
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(SharedPreferenceDisplayKey.CURRENCY_CODE.name, value)
            editor.apply()
        }

    override var currencySymbol: String
        get() = sharedPreferences.getString(SharedPreferenceDisplayKey.CURRENCY_SYMBOL.name, "£") ?: "£"
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(SharedPreferenceDisplayKey.CURRENCY_SYMBOL.name, value)
            editor.apply()
        }

    override var showGridTotals: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_GRID_TOTALS.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_GRID_TOTALS.name, value)
            editor.apply()
        }

    override var showHomeTotal: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_HOME_TOTAL.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_HOME_TOTAL.name, value)
            editor.apply()
        }

    override var showInverterIcon: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_INVERTER_ICON.name, true)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_INVERTER_ICON.name, value)
            editor.apply()
        }

    override var showUsableBatteryOnly: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_USABLE_BATTERY_ONLY.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_USABLE_BATTERY_ONLY.name, value)
            editor.apply()
        }

    override var showBatteryEstimate: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_BATTERY_ESTIMATE.name, true)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_BATTERY_ESTIMATE.name, value)
            editor.apply()
        }

    override var selfSufficiencyEstimateMode: Int
        get() = sharedPreferences.getInt(SharedPreferenceDisplayKey.SELF_SUFFICIENCY_ESTIMATE_MODE.name, 0)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(SharedPreferenceDisplayKey.SELF_SUFFICIENCY_ESTIMATE_MODE.name, value)
            editor.apply()
        }

    override var showSunnyBackground: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_SUNNY_BACKGROUND.name, true)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_SUNNY_BACKGROUND.name, value)
            editor.apply()
        }

    override var decimalPlaces: Int
        get() = sharedPreferences.getInt(SharedPreferenceDisplayKey.DECIMAL_PLACES.name, 2)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(SharedPreferenceDisplayKey.DECIMAL_PLACES.name, value)
            editor.apply()
        }

    override var selectedDeviceSN: String?
        get() = sharedPreferences.getString(SharedPreferenceDeviceKey.SELECTED_DEVICE_SN.name, null)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(SharedPreferenceDeviceKey.SELECTED_DEVICE_SN.name, value)
            editor.apply()
        }

    override var devices: String?
        get() = sharedPreferences.getString(SharedPreferenceDeviceKey.DEVICES.name, null)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(SharedPreferenceDeviceKey.DEVICES.name, value)
            editor.apply()
        }

    override var refreshFrequency: Int
        get() = sharedPreferences.getInt(SharedPreferenceDisplayKey.REFRESH_FREQUENCY.name, 0)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(SharedPreferenceDisplayKey.REFRESH_FREQUENCY.name, value)
            editor.apply()
        }

    override var showBatteryTemperature: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_BATTERY_TEMPERATURE.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_BATTERY_TEMPERATURE.name, value)
            editor.apply()
        }

    override var useColouredFlowLines: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.USE_COLOURED_FLOW_LINES.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.USE_COLOURED_FLOW_LINES.name, value)
            editor.apply()
        }

    override var useLargeDisplay: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.USE_LARGE_DISPLAY.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.USE_LARGE_DISPLAY.name, value)
            editor.apply()
        }

    override var isDemoUser: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDeviceKey.IS_DEMO_USER.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDeviceKey.IS_DEMO_USER.name, value)
            editor.apply()
        }

    override var showFinancialSummary: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_ESTIMATED_EARNINGS.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_ESTIMATED_EARNINGS.name, value)
            editor.apply()
        }

    override var displayUnit: Int
        get() = sharedPreferences.getInt(SharedPreferenceDisplayKey.DISPLAY_UNIT.name, 0)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(SharedPreferenceDisplayKey.DISPLAY_UNIT.name, value)
            editor.apply()
        }

    override var showInverterTemperatures: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_INVERTER_TEMPERATURES.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_INVERTER_TEMPERATURES.name, value)
            editor.apply()
        }

    override var selectedParameterGraphVariables: List<String>
        get() {
            val variables = sharedPreferences.getString(SharedPreferenceDisplayKey.SELECTED_PARAMETER_GRAPH_VARIABLES.name, Gson().toJson(listOf<String>()))
            return Gson().fromJson(variables, object : TypeToken<List<String>>() {}.type)
        }
        set(value) {
            val editor = sharedPreferences.edit()
            val jsonString = Gson().toJson(value)
            editor.putString(SharedPreferenceDisplayKey.SELECTED_PARAMETER_GRAPH_VARIABLES.name, jsonString)
            editor.apply()
        }

    override var shouldInvertCT2: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOULD_INVERT_CT2.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOULD_INVERT_CT2.name, value)
            editor.apply()
        }

    override var showInverterTypeNameOnPowerflow: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_INVERTER_TYPE_NAME_ON_POWERFLOW.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_INVERTER_TYPE_NAME_ON_POWERFLOW.name, value)
            editor.apply()
        }

    override var showInverterStationNameOnPowerflow: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_INVERTER_PLANT_NAME_ON_POWERFLOW.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_INVERTER_PLANT_NAME_ON_POWERFLOW.name, value)
            editor.apply()
        }

    override var deviceBatteryOverrides: Map<String, String>
        get() {
            val variables = sharedPreferences.getString(SharedPreferenceDeviceKey.BATTERY_CAPACITY_OVERRIDES.name, Gson().toJson(listOf<String>()))
            return Gson().fromJson(variables, object : TypeToken<Map<String, String>>() {}.type)
        }
        set(value) {
            val editor = sharedPreferences.edit()
            val jsonString = Gson().toJson(value)
            editor.putString(SharedPreferenceDeviceKey.BATTERY_CAPACITY_OVERRIDES.name, jsonString)
            editor.apply()
        }

    override var showLastUpdateTimestamp: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_LAST_UPDATE_TIMESTAMP.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_LAST_UPDATE_TIMESTAMP.name, value)
            editor.apply()
        }

    override var solarRangeDefinitions: SolarRangeDefinitions
        get() {
            val data = sharedPreferences.getString(SharedPreferenceDisplayKey.SOLAR_RANGE_DEFINITIONS.name, Gson().toJson(SolarRangeDefinitions.defaults))
            return Gson().fromJson(data, object : TypeToken<SolarRangeDefinitions>() {}.type)
        }
        set(value) {
            val editor = sharedPreferences.edit()
            val jsonString = Gson().toJson(value)
            editor.putString(SharedPreferenceDisplayKey.SOLAR_RANGE_DEFINITIONS.name, jsonString)
            editor.apply()
        }

    override var parameterGroups: List<ParameterGroup>
        get() {
            var data = sharedPreferences.getString(SharedPreferenceDisplayKey.PARAMETER_GROUPS.name, null)
            if (data == null) {
                data = Gson().toJson(ParameterGroup.defaults)
                parameterGroups = ParameterGroup.defaults
            }

            return Gson().fromJson(data, object : TypeToken<List<ParameterGroup>>() {}.type)
        }
        set(value) {
            val editor = sharedPreferences.edit()
            val jsonString = Gson().toJson(value)
            editor.putString(SharedPreferenceDisplayKey.PARAMETER_GROUPS.name, jsonString)
            editor.apply()
        }

    override var solcastSettings: SolcastSettings
        get() {
            var data = sharedPreferences.getString(SharedPreferenceDisplayKey.SOLCAST_SETTINGS.name, null)
            if (data == null) {
                data = Gson().toJson(SolcastSettings.defaults)
                solcastSettings = SolcastSettings.defaults
            }

            return Gson().fromJson(data, object : TypeToken<SolcastSettings>() {}.type)
        }
        set(value) {
            val editor = sharedPreferences.edit()
            val jsonString = Gson().toJson(value)
            editor.putString(SharedPreferenceDisplayKey.SOLCAST_SETTINGS.name, jsonString)
            editor.apply()
        }

    override var dataCeiling: Int
        get() = sharedPreferences.getInt(SharedPreferenceDisplayKey.DATA_CEILING.name, DataCeiling.Mild.value)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(SharedPreferenceDisplayKey.DATA_CEILING.name, value)
            editor.apply()
        }

    override var totalYieldModel: Int
        get() = sharedPreferences.getInt(SharedPreferenceDisplayKey.TOTAL_YIELD_MODEL.name, 0)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(SharedPreferenceDisplayKey.TOTAL_YIELD_MODEL.name, value)
            editor.apply()
        }

    override var showFinancialSummaryOnFlowPage: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_FINANCIAL_SUMMARY_ON_POWERFLOW.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_FINANCIAL_SUMMARY_ON_POWERFLOW.name, value)
            editor.apply()
        }

    override var useTraditionalLoadFormula: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.USE_TRADITIONAL_LOAD_FORMULA.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.USE_TRADITIONAL_LOAD_FORMULA.name, value)
            editor.apply()
        }

    override var powerFlowStrings: PowerFlowStringsSettings
        get() {
            var data = sharedPreferences.getString(SharedPreferenceDisplayKey.POWER_FLOW_STRINGS.name, null)
            if (data == null) {
                data = Gson().toJson(PowerFlowStringsSettings.defaults)
                powerFlowStrings = PowerFlowStringsSettings.defaults
            }

            return Gson().fromJson(data, object : TypeToken<PowerFlowStringsSettings>() {}.type)
        }
        set(value) {
            val editor = sharedPreferences.edit()
            val jsonString = Gson().toJson(value)
            editor.putString(SharedPreferenceDisplayKey.POWER_FLOW_STRINGS.name, jsonString)
            editor.apply()
        }

    override var separateParameterGraphsByUnit: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SEPARATE_PARAMETER_GRAPHS_BY_UNIT.name, true)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SEPARATE_PARAMETER_GRAPHS_BY_UNIT.name, value)
            editor.apply()
        }

    override var showBatterySOCAsPercentage: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_BATTERY_SOC_AS_PERCENTAGE.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_BATTERY_SOC_AS_PERCENTAGE.name, value)
            editor.apply()
        }

    override var variables: List<Variable>
        get() {
            var data = sharedPreferences.getString(SharedPreferenceDisplayKey.VARIABLES.name, null)
            if (data == null) {
                data = Gson().toJson(listOf<Variable>())
                variables = listOf()
            }

            return Gson().fromJson(data, object : TypeToken<List<Variable>>() {}.type)
        }
        set(value) {
            val editor = sharedPreferences.edit()
            val jsonString = Gson().toJson(value)
            editor.putString(SharedPreferenceDisplayKey.VARIABLES.name, jsonString)
            editor.apply()
        }

    override var showBatteryTimeEstimateOnWidget: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_ESTIMATED_TIME_ON_WIDGET.name, true)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_ESTIMATED_TIME_ON_WIDGET.name, value)
            editor.apply()
        }

    override var showSelfSufficiencyStatsGraphOverlay: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_SELF_SUFFICIENCY_STATS_GRAPH_OVERLAY.name, true)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_SELF_SUFFICIENCY_STATS_GRAPH_OVERLAY.name, value)
            editor.apply()
        }

    override var scheduleTemplates: List<ScheduleTemplate>
        get() {
            var data = sharedPreferences.getString(SharedPreferenceDisplayKey.SCHEDULE_TEMPLATES.name, null)
            if (data == null) {
                data = Gson().toJson(listOf<ScheduleTemplate>())
                scheduleTemplates = listOf()
            }

            return Gson().fromJson(data, object : TypeToken<List<ScheduleTemplate>>() {}.type)
        }
        set(value) {
            val editor = sharedPreferences.edit()
            val jsonString = Gson().toJson(value)
            editor.putString(SharedPreferenceDisplayKey.SCHEDULE_TEMPLATES.name, jsonString)
            editor.apply()
        }

    override var truncatedYAxisOnParameterGraphs: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.TRUNCATED_Y_AXIS_ON_PARAMETER_GRAPHS.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.TRUNCATED_Y_AXIS_ON_PARAMETER_GRAPHS.name, value)
            editor.apply()
        }

    override var earningsModel: Int
        get() = sharedPreferences.getInt(SharedPreferenceDisplayKey.EARNINGS_MODEl.name, EarningsModel.Exported.value)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(SharedPreferenceDisplayKey.EARNINGS_MODEl.name, value)
            editor.apply()
        }

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
                SummaryDateRange.Manual(from, to)
            }
        }
        set(value) {
            val editor = sharedPreferences.edit()
            val serialisedValue = when (value) {
                is SummaryDateRange.Automatic -> SummaryDateRangeSerialised(automatic = true, from = null, to = null)
                is SummaryDateRange.Manual -> SummaryDateRangeSerialised(automatic = false, from = value.from, to = value.to)
            }

            val jsonString = Gson().toJson(serialisedValue)
            editor.putString(SharedPreferenceDisplayKey.SUMMARY_DATE_RANGE.name, jsonString)
            editor.apply()
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
            val editor = sharedPreferences.edit()
            if (value != null) {
                val epochMillis = value.toInstant(ZoneOffset.UTC).toEpochMilli()
                editor.putLong(SharedPreferenceDisplayKey.LAST_SOLCAST_REFRESH.name, epochMillis)
            } else {
                editor.remove(SharedPreferenceDisplayKey.LAST_SOLCAST_REFRESH.name)
            }
            editor.apply()
        }

    override var batteryViewModel: BatteryViewModel?
        get() {
            val data: String = sharedPreferences.getString(SharedPreferenceDisplayKey.BATTERY_VIEW_MODEL.name, null) ?: return null

            return Gson().fromJson(data, object : TypeToken<BatteryViewModel>() {}.type)
        }
        set(value) {
            val editor = sharedPreferences.edit()
            val jsonString = Gson().toJson(value)
            editor.putString(SharedPreferenceDisplayKey.BATTERY_VIEW_MODEL.name, jsonString)
            editor.apply()
        }

    override var widgetTapAction: Int
        get() = sharedPreferences.getInt(SharedPreferenceDisplayKey.WIDGET_TAP_ACTION.name, WidgetTapAction.Launch.value)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(SharedPreferenceDisplayKey.WIDGET_TAP_ACTION.name, value)
            editor.apply()
        }

    override var batteryTemperatureDisplayMode: Int
        get() = sharedPreferences.getInt(SharedPreferenceDisplayKey.BATTERY_TEMPERATURE_DISPLAY_MODE.name, BatteryTemperatureDisplayMode.Automatic.value)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(SharedPreferenceDisplayKey.BATTERY_TEMPERATURE_DISPLAY_MODE.name, value)
            editor.apply()
        }

    override var showInverterScheduleQuickLink: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_INVERTER_SCHEDULE_QUICK_LINK.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_INVERTER_SCHEDULE_QUICK_LINK.name, value)
            editor.apply()
        }

    override var fetchSolcastOnAppLaunch: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.FETCH_SOLCAST_ON_APP_LAUNCH.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.FETCH_SOLCAST_ON_APP_LAUNCH.name, value)
            editor.apply()
        }

    override var showCT2ValueAsString: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceDisplayKey.SHOW_CT2_VALUE_AS_STRING.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceDisplayKey.SHOW_CT2_VALUE_AS_STRING.name, value)
            editor.apply()
        }
}