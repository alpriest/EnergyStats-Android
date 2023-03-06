package com.alpriest.energystats.stores

import android.content.SharedPreferences
import com.alpriest.energystats.models.ConfigInterface

class SharedPreferencesConfigStore(private val sharedPreferences: SharedPreferences) :
    ConfigInterface {

    private enum class SharedPreferenceKey {
        BATTERY_CAPACITY,
        MIN_SOC,
        DEVICE_ID,
        DEVICE_SN,
        HAS_BATTERY,
        HAS_PV,
        IS_DEMO_USER,
        USE_LARGE_DISPLAY,
        USE_COLOURED_FLOW_LINES,
        SHOW_BATTERY_TEMPERATURE,
        REFRESH_FREQUENCY
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

    override var minSOC: String?
        get() = sharedPreferences.getString(SharedPreferenceKey.MIN_SOC.name, null)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(SharedPreferenceKey.MIN_SOC.name, value)
            editor.apply()
        }

    override var batteryCapacityW: String?
        get() = sharedPreferences.getString(SharedPreferenceKey.BATTERY_CAPACITY.name, null)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(SharedPreferenceKey.BATTERY_CAPACITY.name, value)
            editor.apply()
        }

    override var deviceID: String?
        get() = sharedPreferences.getString(SharedPreferenceKey.DEVICE_ID.name, null)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(SharedPreferenceKey.DEVICE_ID.name, value)
            editor.apply()
        }

    override var deviceSN: String?
        get() = sharedPreferences.getString(SharedPreferenceKey.DEVICE_SN.name, null)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(SharedPreferenceKey.DEVICE_SN.name, value)
            editor.apply()
        }

    override var hasBattery: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.HAS_BATTERY.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceKey.HAS_BATTERY.name, value)
            editor.apply()
        }

    override var hasPV: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.HAS_PV.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceKey.HAS_PV.name, value)
            editor.apply()
        }

    override var isDemoUser: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.IS_DEMO_USER.name, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SharedPreferenceKey.IS_DEMO_USER.name, value)
            editor.apply()
        }
}