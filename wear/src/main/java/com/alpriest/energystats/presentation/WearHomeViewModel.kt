package com.alpriest.energystats.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.alpriest.energystats.sync.SharedPreferencesConfigStore
import com.alpriest.energystats.sync.make
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    }
}