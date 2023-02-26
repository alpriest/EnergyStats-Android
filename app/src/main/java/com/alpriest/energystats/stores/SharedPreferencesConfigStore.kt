package com.alpriest.energystats.stores

import android.content.SharedPreferences
import com.alpriest.energystats.models.ConfigInterface

class SharedPreferencesConfigStore(private val sharedPreferences: SharedPreferences) :
    ConfigInterface {
    private val BATTERY_CAPACITY = "BATTERY_CAPACITY"
    private val MIN_SOC = "MIN_SOC"
    private val DEVICE_ID = "DEVICE_ID"
    private val DEVICE_SN = "DEVICE_SN"
    private val HAS_BATTERY = "HAS_BATTERY"
    private val HAS_PV = "HAS_PV"
    private val IS_DEMO_USER = "IS_DEMO_USER"
    private val USE_LARGE_DISPlAY = "USE_LARGE_DISPlAY"

    override var useLargeDisplay: Boolean
        get() = sharedPreferences.getBoolean(USE_LARGE_DISPlAY, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(USE_LARGE_DISPlAY, value)
            editor.apply()
        }

    override var minSOC: String?
        get() = sharedPreferences.getString(MIN_SOC, null)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(MIN_SOC, value)
            editor.apply()
        }

    override var batteryCapacityW: String?
        get() = sharedPreferences.getString(BATTERY_CAPACITY, null)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(BATTERY_CAPACITY, value)
            editor.apply()
        }

    override var deviceID: String?
        get() = sharedPreferences.getString(DEVICE_ID, null)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(DEVICE_ID, value)
            editor.apply()
        }

    override var deviceSN: String?
        get() = sharedPreferences.getString(DEVICE_SN, null)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putString(DEVICE_SN, value)
            editor.apply()
        }

    override var hasBattery: Boolean
        get() = sharedPreferences.getBoolean(HAS_BATTERY, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(HAS_BATTERY, value)
            editor.apply()
        }

    override var hasPV: Boolean
        get() = sharedPreferences.getBoolean(HAS_PV, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(HAS_PV, value)
            editor.apply()
        }

    override var isDemoUser: Boolean
        get() = sharedPreferences.getBoolean(IS_DEMO_USER, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(IS_DEMO_USER, value)
            editor.apply()
        }
}