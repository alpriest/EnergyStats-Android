package com.alpriest.energystats.wear.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.shared.models.SharedDataKeys
import com.alpriest.energystats.wear.sync.CREDS_PATH
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WearHomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(WearPowerFlowState(LoadState.Inactive, 0.0, 0.0, 0.0, 0.0))
    val state: StateFlow<WearPowerFlowState> = _state.asStateFlow()

    fun bootstrapFromDataLayer(appContext: Context) {
        viewModelScope.launch {
            val latest = readLatestData(appContext)
            if (latest != null) {
                updateFromDataLayer(latest)
            }
        }
    }

    fun updateFromDataLayer(map: Map<String, Any?>) {
        val solar = map[SharedDataKeys.SOLAR_AMOUNT] as? Double
        val house = map[SharedDataKeys.HOUSE_LOAD_AMOUNT] as? Double
        val battery = map[SharedDataKeys.BATTERY_AMOUNT] as? Double
        val grid = map[SharedDataKeys.GRID_AMOUNT] as? Double

        _state.value = _state.value.copy(
            solarAmount = solar ?: _state.value.solarAmount,
            houseLoadAmount = house ?: _state.value.houseLoadAmount,
            batteryAmount = battery ?: _state.value.batteryAmount,
            gridAmount = grid ?: _state.value.gridAmount,
        )
    }

    private suspend fun readLatestData(appContext: Context): Map<String, Any?>? {
        val dataClient = Wearable.getDataClient(appContext)
        val buffer = dataClient.dataItems.await()

        buffer.use {
            val item = it.firstOrNull { e -> e.uri.path == CREDS_PATH } ?: return null
            val map = DataMapItem.fromDataItem(item).dataMap
            return map.keySet().associateWith { key -> map.get(key) }
        }
    }
}