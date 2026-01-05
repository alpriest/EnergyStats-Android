package com.alpriest.energystats.sync

import android.content.Context
import com.alpriest.energystats.shared.models.SharedDataKeys
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.alpriest.energystats.complication.MainComplicationService
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

const val CREDS_PATH = "/auth/creds"

class CredsListenerService : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type != DataEvent.TYPE_CHANGED) continue
            val item = event.dataItem
            if (item.uri.path != CREDS_PATH) continue

            val map = DataMapItem.fromDataItem(item).dataMap

            val token = map.getString(SharedDataKeys.TOKEN)
            val deviceSN = map.getString(SharedDataKeys.DEVICE_SN)
            val showGridTotalsOnPowerFlow = map.getBoolean(SharedDataKeys.SHOW_GRID_TOTALS, false)
            val batteryCapacity = map.getString(SharedDataKeys.BATTERY_CAPACITY)
            val shouldInvertCT2 = map.getBoolean(SharedDataKeys.SHOULD_INVERT_CT2, false)
            val minSOC = map.getDouble(SharedDataKeys.MIN_SOC, 0.0)
            val shouldCombineCT2WithPVPower = map.getBoolean(SharedDataKeys.SHOULD_COMBINE_CT2_WITH_PV, false)
            val showUsableBatteryOnly = map.getBoolean(SharedDataKeys.SHOW_USABLE_BATTERY_ONLY, false)

            val solarRange = map.getDataMap(SharedDataKeys.SOLAR_RANGE_DEFINITIONS)
            val threshold1 = solarRange?.getDouble(SharedDataKeys.THRESHOLD_1)
            val threshold2 = solarRange?.getDouble(SharedDataKeys.THRESHOLD_2)
            val threshold3 = solarRange?.getDouble(SharedDataKeys.THRESHOLD_3)

            saveToWatchStorage(
                this,
                token,
                deviceSN,
                showGridTotalsOnPowerFlow,
                batteryCapacity,
                shouldInvertCT2,
                minSOC,
                shouldCombineCT2WithPVPower,
                showUsableBatteryOnly,
                threshold1,
                threshold2,
                threshold3
            )

            MainComplicationService.requestRefresh(applicationContext)
        }
    }

    private fun saveToWatchStorage(
        context: Context,
        token: String?,
        deviceSN: String?,
        showGridTotalsOnPowerFlow: Boolean,
        batteryCapacity: String?,
        shouldInvertCT2: Boolean,
        minSOC: Double,
        shouldCombineCT2WithPVPower: Boolean,
        showUsableBatteryOnly: Boolean,
        threshold1: Double?,
        threshold2: Double?,
        threshold3: Double?
    ) {
        val store = SharedPreferencesConfigStore.make(context)
        store.token = token
        store.selectedDeviceSN = deviceSN
        store.showGridTotals = showGridTotalsOnPowerFlow
        store.batteryCapacity = batteryCapacity
        store.shouldInvertCT2 = shouldInvertCT2
        store.minSOC = minSOC
        store.shouldCombineCT2WithPVPower = shouldCombineCT2WithPVPower
        store.showUsableBatteryOnly = showUsableBatteryOnly
        val solarRangeDefinitions = listOf(threshold1, threshold2, threshold3)
            .takeIf { it.all { v -> v != null } }
            ?.let { (t1, t2, t3) ->
                SolarRangeDefinitions(t1!!, t2!!, t3!!)
            }

        solarRangeDefinitions?.let {
            store.solarRangeDefinitions = it
        }
    }
}
