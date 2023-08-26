package com.alpriest.energystats.stores

import android.content.SharedPreferences
import com.alpriest.energystats.models.ConfigInterface
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesConfigStore(private val sharedPreferences: SharedPreferences) :
    ConfigInterface {

    private enum class SharedPreferenceKey {
        IS_DEMO_USER,
        USE_LARGE_DISPLAY,
        USE_COLOURED_FLOW_LINES,
        SHOW_BATTERY_TEMPERATURE,
        REFRESH_FREQUENCY,
        SELECTED_DEVICE_ID,
        DEVICES,
        SHOW_SUNNY_BACKGROUND,
        DECIMAL_PLACES,
        SHOW_BATTERY_ESTIMATE,
        SHOW_USABLE_BATTERY_ONLY,
        SHOW_TOTAL_YIELD,
        SELF_SUFFICIENCY_ESTIMATE_MODE,
        SHOW_ESTIMATED_EARNINGS,
        SHOW_VALUES_IN_WATTS,
        SHOW_INVERTER_TEMPERATURES,
        SELECTED_PARAMETER_GRAPH_VARIABLES
    }

    override var showTotalYield: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.SHOW_TOTAL_YIELD.name, true)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceKey.SHOW_TOTAL_YIELD.name, value)
            editor.apply()
        }

    override var showUsableBatteryOnly: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.SHOW_USABLE_BATTERY_ONLY.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceKey.SHOW_USABLE_BATTERY_ONLY.name, value)
            editor.apply()
        }

    override var showBatteryEstimate: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.SHOW_BATTERY_ESTIMATE.name, true)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceKey.SHOW_BATTERY_ESTIMATE.name, value)
            editor.apply()
        }

    override var selfSufficiencyEstimateMode: Int
        get() = sharedPreferences.getInt(SharedPreferenceKey.SELF_SUFFICIENCY_ESTIMATE_MODE.name, 0)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(SharedPreferenceKey.SELF_SUFFICIENCY_ESTIMATE_MODE.name, value)
            editor.apply()
        }

    override var showSunnyBackground: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.SHOW_SUNNY_BACKGROUND.name, true)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceKey.SHOW_SUNNY_BACKGROUND.name, value)
            editor.apply()
        }

    override var decimalPlaces: Int
        get() = sharedPreferences.getInt(SharedPreferenceKey.DECIMAL_PLACES.name, 2)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(SharedPreferenceKey.DECIMAL_PLACES.name, value)
            editor.apply()
        }

    override var selectedDeviceID: String?
        get() = sharedPreferences.getString(SharedPreferenceKey.SELECTED_DEVICE_ID.name, null)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(SharedPreferenceKey.SELECTED_DEVICE_ID.name, value)
            editor.apply()
        }

    override var devices: String?
        get() = sharedPreferences.getString(SharedPreferenceKey.DEVICES.name, null)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(SharedPreferenceKey.DEVICES.name, value)
            editor.apply()
        }

    override var refreshFrequency: Int
        get() = sharedPreferences.getInt(SharedPreferenceKey.REFRESH_FREQUENCY.name, 0)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(SharedPreferenceKey.REFRESH_FREQUENCY.name, value)
            editor.apply()
        }

    override var showBatteryTemperature: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.SHOW_BATTERY_TEMPERATURE.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceKey.SHOW_BATTERY_TEMPERATURE.name, value)
            editor.apply()
        }

    override var useColouredFlowLines: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.USE_COLOURED_FLOW_LINES.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceKey.USE_COLOURED_FLOW_LINES.name, value)
            editor.apply()
        }

    override var useLargeDisplay: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.USE_LARGE_DISPLAY.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceKey.USE_LARGE_DISPLAY.name, value)
            editor.apply()
        }

    override var isDemoUser: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.IS_DEMO_USER.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceKey.IS_DEMO_USER.name, value)
            editor.apply()
        }

    override var showEstimatedEarnings: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.SHOW_ESTIMATED_EARNINGS.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceKey.SHOW_ESTIMATED_EARNINGS.name, value)
            editor.apply()
        }

    override var showValuesInWatts: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.SHOW_VALUES_IN_WATTS.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceKey.SHOW_VALUES_IN_WATTS.name, value)
            editor.apply()
        }

    override var showInverterTemperatures: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.SHOW_INVERTER_TEMPERATURES.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceKey.SHOW_INVERTER_TEMPERATURES.name, value)
            editor.apply()
        }

    override var selectedParameterGraphVariables: List<String>
        get() {
            val variables = sharedPreferences.getString(SharedPreferenceKey.SELECTED_PARAMETER_GRAPH_VARIABLES.name, Gson().toJson(listOf<String>()))
            return Gson().fromJson(variables, object: TypeToken<List<String>>() {}.type)
        }
        set(value) {
            val editor = sharedPreferences.edit()
            val jsonString = Gson().toJson(value)
            editor.putString(SharedPreferenceKey.SELECTED_PARAMETER_GRAPH_VARIABLES.name, jsonString)
            editor.apply()
        }
}