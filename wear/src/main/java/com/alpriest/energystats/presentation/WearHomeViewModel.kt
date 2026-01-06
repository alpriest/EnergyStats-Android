package com.alpriest.energystats.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.shared.models.DataCeiling
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.alpriest.energystats.shared.network.FoxAPIService
import com.alpriest.energystats.shared.network.NetworkCache
import com.alpriest.energystats.shared.network.NetworkFacade
import com.alpriest.energystats.shared.network.NetworkService
import com.alpriest.energystats.shared.network.NetworkValueCleaner
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.shared.network.RequestData
import com.alpriest.energystats.sync.SharedPreferencesConfigStore
import com.alpriest.energystats.sync.make
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class WearHomeViewModel(application: Application) : AndroidViewModel(application) {
    val store = SharedPreferencesConfigStore.make(application)

    private val _state = MutableStateFlow(
        WearPowerFlowState(
            LoadState.Inactive,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            SolarRangeDefinitions.defaults
        )
    )
    val state: StateFlow<WearPowerFlowState> = _state.asStateFlow()

    init {
        // Bind to the store
        viewModelScope.launch {
            store.updatesFlow().collect { snapshot ->
                _state.value = _state.value.copy(
                    solarAmount = snapshot.solarGenerationAmount,
                    houseLoadAmount = snapshot.houseLoadAmount,
                    batteryChargeLevel = snapshot.batteryChargeLevel,
                    batteryChargeAmount = snapshot.batteryChargeAmount,
                    gridAmount = snapshot.gridAmount,
                    solarRangeDefinitions = snapshot.solarRangeDefinitions,
                )
            }
        }

        // Fetch new values into the store
//        viewModelScope.launch {
//            val network = FoxAPIService()
        // Fetch data
        // Bind into store
//        }
    }

    suspend fun load() {
        val deviceSN = store.selectedDeviceSN ?: return
        val now = Instant.now()
        if (store.lastRefreshTime.isAfter(now.minusSeconds(4 * 60)) || store.apiKey == null) {
            return
        }

        val reals = networking.fetchRealData(
            deviceSN,
            listOf(
                "SoC",
                "SoC_1",
                "pvPower",
                "feedinPower",
                "gridConsumptionPower",
                "generationPower",
                "meterPower2",
                "batChargePower",
                "batDischargePower",
                "ResidualEnergy",
                "batTemperature",
                "batTemperature_1",
                "batTemperature_2"
            )
        )
    }

    val networking: Networking by lazy {
        val requestData = RequestData(
            apiKey = { store.apiKey ?: "" },
            userAgent = "Energy Stats Android"
        )

        NetworkService(
            NetworkValueCleaner(
                NetworkFacade(
                    api = NetworkCache(api = FoxAPIService(requestData)),
                    isDemoUser = { false }
                ),
                { DataCeiling.None }
            )
        )
    }
}