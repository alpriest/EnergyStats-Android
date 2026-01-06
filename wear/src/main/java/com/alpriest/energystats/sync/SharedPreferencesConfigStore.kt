package com.alpriest.energystats.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.Instant

data class WearCredsSnapshot(
    val token: String?,
    val selectedDeviceSN: String?,
    val batteryCapacity: String?,
    val showGridTotals: Boolean,
    val shouldInvertCT2: Boolean,
    val minSOC: Double,
    val shouldCombineCT2WithPVPower: Boolean,
    val showUsableBatteryOnly: Boolean,
    val solarRangeDefinitions: SolarRangeDefinitions,
    val solarGenerationAmount: Double,
    val houseLoadAmount: Double,
    val batteryChargeLevel: Double,
    val batteryChargeAmount: Double,
    val gridAmount: Double
)

fun SharedPreferencesConfigStore.snapshot(): WearCredsSnapshot = WearCredsSnapshot(
    token = apiKey,
    selectedDeviceSN = selectedDeviceSN,
    batteryCapacity = batteryCapacity,
    showGridTotals = showGridTotals,
    shouldInvertCT2 = shouldInvertCT2,
    minSOC = minSOC,
    shouldCombineCT2WithPVPower = shouldCombineCT2WithPVPower,
    showUsableBatteryOnly = showUsableBatteryOnly,
    solarRangeDefinitions = solarRangeDefinitions,
    solarGenerationAmount = solarGenerationAmount,
    houseLoadAmount = houseLoadAmount,
    batteryChargeLevel = batteryChargeLevel,
    batteryChargeAmount = batteryChargeAmount,
    gridAmount = gridAmount
)

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
        SOLAR_GENERATION_AMOUNT,
        HOUSE_LOAD_AMOUNT,
        BATTERY_CHARGE_LEVEL,
        BATTERY_CHARGE_AMOUNT,
        GRID_AMOUNT,
        LAST_REFRESH_TIME
    }

    var lastRefreshTime: Instant
        get() {
            val millis = sharedPreferences.getLong(SharedPreferenceKey.LAST_REFRESH_TIME.name, 0L)
            return if (millis == 0L) {
                Instant.MAX
            } else {
                Instant.ofEpochMilli(millis)
            }
        }
        set(value) {
            sharedPreferences.edit {
                putLong(
                    SharedPreferenceKey.LAST_REFRESH_TIME.name,
                    value.toEpochMilli()
                )
            }
        }

    var apiKey: String?
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

    var solarGenerationAmount: Double
        get() = (sharedPreferences.getString(SharedPreferenceKey.SOLAR_GENERATION_AMOUNT.name, "0.0") ?: "0.0").toDouble()
        set(value) {
            sharedPreferences.edit {
                putString(SharedPreferenceKey.SOLAR_GENERATION_AMOUNT.name, value.toString())
            }
        }

    var houseLoadAmount: Double
        get() = (sharedPreferences.getString(SharedPreferenceKey.HOUSE_LOAD_AMOUNT.name, "0.0") ?: "0.0").toDouble()
        set(value) {
            sharedPreferences.edit {
                putString(SharedPreferenceKey.HOUSE_LOAD_AMOUNT.name, value.toString())
            }
        }

    var batteryChargeAmount: Double
        get() = (sharedPreferences.getString(SharedPreferenceKey.BATTERY_CHARGE_AMOUNT.name, "0.0") ?: "0.0").toDouble()
        set(value) {
            sharedPreferences.edit {
                putString(SharedPreferenceKey.BATTERY_CHARGE_AMOUNT.name, value.toString())
            }
        }

    var batteryChargeLevel: Double
        get() = (sharedPreferences.getString(SharedPreferenceKey.BATTERY_CHARGE_LEVEL.name, "0.0") ?: "0.0").toDouble()
        set(value) {
            sharedPreferences.edit {
                putString(SharedPreferenceKey.BATTERY_CHARGE_LEVEL.name, value.toString())
            }
        }

    var gridAmount: Double
        get() = (sharedPreferences.getString(SharedPreferenceKey.GRID_AMOUNT.name, "0.0") ?: "0.0").toDouble()
        set(value) {
            sharedPreferences.edit {
                putString(SharedPreferenceKey.GRID_AMOUNT.name, value.toString())
            }
        }

    fun updatesFlow(): Flow<WearCredsSnapshot> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            trySend(snapshot())
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

        // Emit immediately so UI has the current values
        trySend(snapshot())

        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    companion object
}

fun SharedPreferencesConfigStore.Companion.make(context: Context): SharedPreferencesConfigStore {
    val preferences = context.getSharedPreferences(
        "com.alpriest.energystats", Context.MODE_PRIVATE
    )
    return SharedPreferencesConfigStore(preferences)
}
