package com.alpriest.energystats.wear.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class SharedPreferencesConfigStore(private val sharedPreferences: SharedPreferences) {
    private enum class SharedPreferenceKey {
        TOKEN,
        SELECTED_DEVICE_SN,
        BATTERY_CAPACITY,
        SHOW_GRID_TOTALS,
        SHOULD_INVERT_CT2,
        MIN_SOC,
        SHOULD_COMBINE_CT2_WITH_PVPOWER,
        SHOW_USABLE_BATTERY_ONLY,
        SOLAR_RANGE_DEFINITIONS,
    }

    var token: String?
        get() = sharedPreferences.getString(SharedPreferenceKey.TOKEN.name, null)
        set(value) {
            sharedPreferences.edit {
                putString(SharedPreferenceKey.TOKEN.name, value)
            }
        }

    var showGridTotals: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.SHOW_GRID_TOTALS.name, false)
        set(value) {
            sharedPreferences.edit {
                putBoolean(SharedPreferenceKey.SHOW_GRID_TOTALS.name, value)
            }
        }

    var selectedDeviceSN: String?
        get() = sharedPreferences.getString(SharedPreferenceKey.SELECTED_DEVICE_SN.name, null)
        set(value) {
            sharedPreferences.edit {
                putString(SharedPreferenceKey.SELECTED_DEVICE_SN.name, value)
            }
        }

    var batteryCapacity: String?
        get() = sharedPreferences.getString(SharedPreferenceKey.BATTERY_CAPACITY.name, null)
        set(value) {
            sharedPreferences.edit {
                putString(SharedPreferenceKey.BATTERY_CAPACITY.name, value)
            }
        }

    var shouldInvertCT2: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.SHOULD_INVERT_CT2.name, false)
        set(value) {
            sharedPreferences.edit {
                putBoolean(SharedPreferenceKey.SHOULD_INVERT_CT2.name, value)
            }
        }

    var minSOC: Double
        get() = (sharedPreferences.getString(SharedPreferenceKey.MIN_SOC.name, "0.1") ?: "0.1").toDouble()
        set(value) {
            sharedPreferences.edit {
                putString(SharedPreferenceKey.MIN_SOC.name, value.toString())
            }
        }

    var shouldCombineCT2WithPVPower: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.SHOULD_COMBINE_CT2_WITH_PVPOWER.name, true)
        set(value) {
            sharedPreferences.edit {
                putBoolean(SharedPreferenceKey.SHOULD_COMBINE_CT2_WITH_PVPOWER.name, value)
            }
        }

    var showUsableBatteryOnly: Boolean
        get() = sharedPreferences.getBoolean(SharedPreferenceKey.SHOW_USABLE_BATTERY_ONLY.name, false)
        set(value) {
            sharedPreferences.edit {
                putBoolean(SharedPreferenceKey.SHOW_USABLE_BATTERY_ONLY.name, value)
            }
        }

    var solarRangeDefinitions: SolarRangeDefinitions
        get() {
            val data = sharedPreferences.getString(SharedPreferenceKey.SOLAR_RANGE_DEFINITIONS.name, Gson().toJson(SolarRangeDefinitions.Companion.defaults))
            return Gson().fromJson(data, object : TypeToken<SolarRangeDefinitions>() {}.type)
        }
        set(value) {
            sharedPreferences.edit {
                val jsonString = Gson().toJson(value)
                putString(SharedPreferenceKey.SOLAR_RANGE_DEFINITIONS.name, jsonString)
            }
        }

    companion object
}

fun SharedPreferencesConfigStore.Companion.make(context: Context): SharedPreferencesConfigStore {
    val preferences = context.getSharedPreferences(
        "com.alpriest.energystats",
        Context.MODE_PRIVATE
    )
    return SharedPreferencesConfigStore(preferences)
}