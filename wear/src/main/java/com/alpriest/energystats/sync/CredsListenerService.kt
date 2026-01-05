package com.alpriest.energystats.sync

import android.annotation.SuppressLint
import com.alpriest.energystats.complication.MainComplicationService
import com.alpriest.energystats.shared.models.SharedDataKeys
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

const val CREDS_PATH = "/auth/creds"

class CredsListenerService : WearableListenerService() {
    @SuppressLint("VisibleForTests")
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
//            if (event.type != DataEvent.TYPE_CHANGED) continue
            val item = event.dataItem
//            if (item.uri.path != CREDS_PATH) continue

            val map = DataMapItem.fromDataItem(item).dataMap

            val token = map.getString(SharedDataKeys.TOKEN)
            val deviceSN = map.getString(SharedDataKeys.DEVICE_SN)
            val showGridTotalsOnPowerFlow = map.getBoolean(SharedDataKeys.SHOW_GRID_TOTALS, false)
            val batteryCapacity = map.getString(SharedDataKeys.BATTERY_CAPACITY)
            val shouldInvertCT2 = map.getBoolean(SharedDataKeys.SHOULD_INVERT_CT2, false)
            val minSOC = map.getDouble(SharedDataKeys.MIN_SOC, 0.0)
            val shouldCombineCT2WithPVPower = map.getBoolean(SharedDataKeys.SHOULD_COMBINE_CT2_WITH_PV, false)
            val showUsableBatteryOnly = map.getBoolean(SharedDataKeys.SHOW_USABLE_BATTERY_ONLY, false)
            val solarGenerationAmount = map.getDouble(SharedDataKeys.SOLAR_GENERATION_AMOUNT, 0.0)
            val houseLoadAmount = map.getDouble(SharedDataKeys.HOUSE_LOAD_AMOUNT, 0.0)
            val batteryChargeLevel = map.getDouble(SharedDataKeys.BATTERY_CHARGE_LEVEL, 0.0)
            val batteryChargeAmount = map.getDouble(SharedDataKeys.BATTERY_CHARGE_AMOUNT, 0.0)
            val gridAmount = map.getDouble(SharedDataKeys.GRID_AMOUNT, 0.0)

            val solarRangeDefinitionsMap = map.getDataMap(SharedDataKeys.SOLAR_RANGE_DEFINITIONS)
            val solarRangeDefinitions: SolarRangeDefinitions = solarRangeDefinitionsMap?.let { dm ->
                // Only construct if at least one threshold value exists in the map.
                val t1 = dm.doubleOrNull(SharedDataKeys.THRESHOLD_1)
                val t2 = dm.doubleOrNull(SharedDataKeys.THRESHOLD_2)
                val t3 = dm.doubleOrNull(SharedDataKeys.THRESHOLD_3)

                if (t1 == null || t2 == null || t3 == null) {
                    SolarRangeDefinitions.defaults
                } else {
                    SolarRangeDefinitions(t1, t2, t3)
                }
            } ?: SolarRangeDefinitions.defaults

            val store = SharedPreferencesConfigStore.make(applicationContext)
            store.token = token
            store.selectedDeviceSN = deviceSN
            store.showGridTotals = showGridTotalsOnPowerFlow
            store.batteryCapacity = batteryCapacity
            store.shouldInvertCT2 = shouldInvertCT2
            store.minSOC = minSOC
            store.shouldCombineCT2WithPVPower = shouldCombineCT2WithPVPower
            store.showUsableBatteryOnly = showUsableBatteryOnly
            store.solarRangeDefinitions = solarRangeDefinitions
            store.solarGenerationAmount = solarGenerationAmount
            store.houseLoadAmount = houseLoadAmount
            store.batteryChargeLevel = batteryChargeLevel
            store.batteryChargeAmount = batteryChargeAmount
            store.gridAmount = gridAmount

            MainComplicationService.requestRefresh(applicationContext)
        }
    }
}

fun DataMap.doubleOrNull(key: String): Double? =
    if (containsKey(key)) getDouble(key) else null
