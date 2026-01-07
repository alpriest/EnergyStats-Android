package com.alpriest.energystats.sync

import android.annotation.SuppressLint
import com.alpriest.energystats.complication.MainComplicationService
import com.alpriest.energystats.shared.models.SharedDataKeys
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

const val CREDS_PATH = "/auth/creds"

class CredsListenerService : WearableListenerService() {
    @SuppressLint("VisibleForTests")
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type != DataEvent.TYPE_CHANGED) continue
            val item = event.dataItem
            if (item.uri.path != CREDS_PATH) continue

            val map = DataMapItem.fromDataItem(item).dataMap
            val store = SharedPreferencesConfigStore.make(applicationContext)

            store.applyAndNotify {
                map.getString(SharedDataKeys.TOKEN)?.let {
                    apiKey = it
                }
                map.getString(SharedDataKeys.DEVICE_SN)?.let {
                    selectedDeviceSN = it
                }
                showGridTotals = map.getBoolean(SharedDataKeys.SHOW_GRID_TOTALS, false)
                batteryCapacity = map.getString(SharedDataKeys.BATTERY_CAPACITY)
                shouldInvertCT2 = map.getBoolean(SharedDataKeys.SHOULD_INVERT_CT2, false)
                minSOC = map.getDouble(SharedDataKeys.MIN_SOC, 0.0)
                shouldCombineCT2WithPVPower = map.getBoolean(SharedDataKeys.SHOULD_COMBINE_CT2_WITH_PV, false)
                showUsableBatteryOnly = map.getBoolean(SharedDataKeys.SHOW_USABLE_BATTERY_ONLY, false)
                solarGenerationAmount = map.getDouble(SharedDataKeys.SOLAR_GENERATION_AMOUNT, 0.0)
                houseLoadAmount = map.getDouble(SharedDataKeys.HOUSE_LOAD_AMOUNT, 0.0)
                batteryChargeLevel = map.getDouble(SharedDataKeys.BATTERY_CHARGE_LEVEL, 0.0)
                batteryChargeAmount = map.getDouble(SharedDataKeys.BATTERY_CHARGE_AMOUNT, 0.0)
                gridAmount = map.getDouble(SharedDataKeys.GRID_AMOUNT, 0.0)

                val solarRangeDefinitionsMap = map.getDataMap(SharedDataKeys.SOLAR_RANGE_DEFINITIONS)
                store.solarRangeDefinitions = solarRangeDefinitionsMap?.let { dm ->
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
            }

            MainComplicationService.requestRefresh(applicationContext)
        }
    }
}

fun DataMap.doubleOrNull(key: String): Double? =
    if (containsKey(key)) getDouble(key) else null
