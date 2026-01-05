package com.alpriest.energystats.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.shared.models.SharedDataKeys
import com.alpriest.energystats.sync.CREDS_PATH
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WearHomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(WearPowerFlowState(
        LoadState.Inactive,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0,
        0.0
    ))
    val state: StateFlow<WearPowerFlowState> = _state.asStateFlow()

    fun bootstrapFromDataLayer(appContext: Context) {
        viewModelScope.launch {
            val data = readLatestData(appContext)

            if (data != null) {
                updateFromDataLayer(data)
            }
        }
    }

    fun updateFromDataLayer(map: Map<String, Any?>) {
        val solar = map[SharedDataKeys.SOLAR_GENERATION_AMOUNT] as? Double
        val house = map[SharedDataKeys.HOUSE_LOAD_AMOUNT] as? Double
        val batteryChargeLevel = map[SharedDataKeys.BATTERY_CHARGE_LEVEL] as? Double
        val batteryChargeAmount = map[SharedDataKeys.BATTERY_CHARGE_AMOUNT] as? Double
        val grid = map[SharedDataKeys.GRID_AMOUNT] as? Double
        val threshold1 = map[SharedDataKeys.THRESHOLD_1] as? Double
        val threshold2 = map[SharedDataKeys.THRESHOLD_2] as? Double
        val threshold3 = map[SharedDataKeys.THRESHOLD_3] as? Double

        _state.value = _state.value.copy(
            solarAmount = solar ?: _state.value.solarAmount,
            houseLoadAmount = house ?: _state.value.houseLoadAmount,
            batteryChargeLevel = batteryChargeLevel ?: _state.value.batteryChargeLevel,
            batteryChargeAmount = batteryChargeAmount ?: _state.value.batteryChargeAmount,
            gridAmount = grid ?: _state.value.gridAmount,
            threshold1 = threshold1 ?: _state.value.threshold1,
            threshold2 = threshold2 ?: _state.value.threshold2,
            threshold3 = threshold3 ?: _state.value.threshold3
        )
    }

    @SuppressLint("VisibleForTests")
    private suspend fun readLatestData(appContext: Context): Map<String, Any?>? {
        val dataClient = Wearable.getDataClient(appContext)

        val queryUri = Uri.Builder()
            .scheme(PutDataRequest.WEAR_URI_SCHEME)
            .authority("*")
            .path(CREDS_PATH)
            .build()

        return try {
            val dataItems = dataClient.getDataItems(queryUri).await()
            try {
                val first = dataItems.firstOrNull() ?: return null
                val map = DataMapItem.fromDataItem(first).dataMap
                map.keySet().associateWith { key -> map.get(key) }
            } finally {
                dataItems.release()
            }
        } catch (e: ApiException) {
            // Most commonly thrown when the Data Layer isn't ready/connected yet.
            null
        } catch (t: Throwable) {
            null
        }
    }
}