package com.alpriest.energystats.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.alpriest.energystats.shared.helpers.nullableDoublePreference
import com.alpriest.energystats.shared.helpers.preference
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.Instant

data class WearCredsSnapshot(
    val apiKey: String?,
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
    val gridAmount: Double,
    val totalImport: Double?,
    val totalExport: Double?,
    val lastRefreshTime: Instant
)

fun SharedPreferencesConfigStore.snapshot(): WearCredsSnapshot = WearCredsSnapshot(
    apiKey = apiKey,
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
    gridAmount = gridAmount,
    totalImport = totalImport,
    totalExport = totalExport,
    lastRefreshTime = lastRefreshTime
)

class SharedPreferencesConfigStore(private val sharedPreferences: SharedPreferences) {
    private val _updatesFlow = MutableSharedFlow<WearCredsSnapshot>(replay = 1)

    private enum class SharedPreferenceKey {
        TOKEN,
        SELECTED_DEVICE_SN,
        BATTERY_CAPACITY,
        SHOW_GRID_TOTALS,
        SHOULD_INVERT_CT2,
        MIN_SOC,
        SHOULD_COMBINE_CT2_WITH_PVPOWER,
        SHOULD_COMBINE_CT2_WITH_LOADS,
        SHOW_USABLE_BATTERY_ONLY,
        SOLAR_RANGE_DEFINITIONS,
        SOLAR_GENERATION_AMOUNT,
        HOUSE_LOAD_AMOUNT,
        BATTERY_CHARGE_LEVEL,
        BATTERY_CHARGE_AMOUNT,
        GRID_AMOUNT,
        LAST_REFRESH_TIME,
        ALLOW_NEGATIVE_LOAD,
        TOTAL_IMPORT,
        TOTAL_EXPORT,
        PREFS_UPDATED_AT
    }

    var apiKey: String? by preference(sharedPreferences, SharedPreferenceKey.TOKEN.name, null as String?)
    var showGridTotals: Boolean by preference(sharedPreferences, SharedPreferenceKey.SHOW_GRID_TOTALS.name, false)
    var selectedDeviceSN: String? by preference(sharedPreferences, SharedPreferenceKey.SELECTED_DEVICE_SN.name, null as String?)
    var batteryCapacity: String? by preference(sharedPreferences, SharedPreferenceKey.BATTERY_CAPACITY.name, null as String?)
    var shouldInvertCT2: Boolean by preference(sharedPreferences, SharedPreferenceKey.SHOULD_INVERT_CT2.name, false)
    var allowNegativeLoad: Boolean by preference(sharedPreferences, SharedPreferenceKey.ALLOW_NEGATIVE_LOAD.name, false)
    var minSOC: Double by preference(sharedPreferences, SharedPreferenceKey.MIN_SOC.name, 0.1)
    var shouldCombineCT2WithPVPower: Boolean by preference(sharedPreferences, SharedPreferenceKey.SHOULD_COMBINE_CT2_WITH_PVPOWER.name, true)
    var shouldCombineCT2WithLoadsPower: Boolean by preference(sharedPreferences, SharedPreferenceKey.SHOULD_COMBINE_CT2_WITH_LOADS.name, true)
    var showUsableBatteryOnly: Boolean by preference(sharedPreferences, SharedPreferenceKey.SHOW_USABLE_BATTERY_ONLY.name, false)
    var solarGenerationAmount: Double by preference(sharedPreferences, SharedPreferenceKey.SOLAR_GENERATION_AMOUNT.name, 0.0)
    var houseLoadAmount: Double by preference(sharedPreferences, SharedPreferenceKey.HOUSE_LOAD_AMOUNT.name, 0.0)
    var batteryChargeAmount: Double by preference(sharedPreferences, SharedPreferenceKey.BATTERY_CHARGE_AMOUNT.name, 0.0)
    var batteryChargeLevel: Double by preference(sharedPreferences, SharedPreferenceKey.BATTERY_CHARGE_LEVEL.name, 0.0)
    var gridAmount: Double by preference(sharedPreferences, SharedPreferenceKey.GRID_AMOUNT.name, 0.0)

    var totalImport: Double? by nullableDoublePreference(sharedPreferences, SharedPreferenceKey.TOTAL_IMPORT.name)
    var totalExport: Double? by nullableDoublePreference(sharedPreferences, SharedPreferenceKey.TOTAL_EXPORT.name)

    var solarRangeDefinitions: SolarRangeDefinitions
        get() {
            val data = sharedPreferences.getString(SharedPreferenceKey.SOLAR_RANGE_DEFINITIONS.name, Gson().toJson(SolarRangeDefinitions.Companion.defaults))
            return Gson().fromJson(data, object : TypeToken<SolarRangeDefinitions>() {}.type)
        }
        set(value) {
            sharedPreferences.edit(commit = true) {
                val jsonString = Gson().toJson(value)
                putString(SharedPreferenceKey.SOLAR_RANGE_DEFINITIONS.name, jsonString)
            }
        }

    var lastRefreshTime: Instant
        get() {
            val millis = sharedPreferences.getLong(SharedPreferenceKey.LAST_REFRESH_TIME.name, 0L)
            return if (millis == 0L) {
                Instant.now().minusSeconds(10 * 60)
            } else {
                Instant.ofEpochMilli(millis)
            }
        }
        set(value) {
            sharedPreferences.edit(commit = true) {
                putLong(
                    SharedPreferenceKey.LAST_REFRESH_TIME.name,
                    value.toEpochMilli()
                )
            }
        }

    fun updatesFlow(): Flow<WearCredsSnapshot> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == SharedPreferenceKey.PREFS_UPDATED_AT.name) {
                trySend(snapshot()) // updates
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(snapshot()) // initial
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    fun applyAndNotify(block: SharedPreferencesConfigStore.() -> Unit) {
        apply(block)
        sharedPreferences.edit(commit = true) { putLong(SharedPreferenceKey.PREFS_UPDATED_AT.name, System.currentTimeMillis()) }
    }

    companion object
}

fun SharedPreferencesConfigStore.Companion.make(context: Context): SharedPreferencesConfigStore {
    val preferences = context.getSharedPreferences(
        "com.alpriest.energystats", Context.MODE_PRIVATE
    )
    return SharedPreferencesConfigStore(preferences)
}
