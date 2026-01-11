package com.alpriest.energystats.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.complication.MainComplicationService
import com.alpriest.energystats.shared.config.CurrentStatusCalculatorConfig
import com.alpriest.energystats.shared.models.AppTheme
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.shared.models.PowerFlowStringsSettings
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.alpriest.energystats.shared.models.demo
import com.alpriest.energystats.shared.models.isActive
import com.alpriest.energystats.sync.SharedPreferencesConfigStore
import com.alpriest.energystats.sync.WearCredsSnapshot
import com.alpriest.energystats.sync.make
import com.alpriest.energystats.sync.snapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class WearHomeViewModel(application: Application) : AndroidViewModel(application) {
    val store = SharedPreferencesConfigStore.make(application)
    private var deviceSN: String? = null
    private var apiKey: String? = null

    private val foregroundObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            viewModelScope.launch {
                load()
            }
        }
    }

    private val _state = MutableStateFlow(
        WearPowerFlowState(
            LoadState.Inactive,
            null,
            null,
            null,
            null,
            null,
            null,
            SolarRangeDefinitions.defaults,
            null,
            null
        )
    )
    val state: StateFlow<WearPowerFlowState> = _state.asStateFlow()

    init {
        bindToState(store.snapshot())

        ProcessLifecycleOwner.get().lifecycle.addObserver(foregroundObserver)

        // Listen for updates
        viewModelScope.launch {
            store.updatesFlow()
                .onEach { snapshot ->
                    val needsLoad = (deviceSN != snapshot.selectedDeviceSN || apiKey != snapshot.apiKey)
                    bindToState(snapshot)

                    if (needsLoad) {
                        load()
                    }
                }
                .catch { e -> e.printStackTrace() }
                .onCompletion { cause -> Log.d("AWP", "updatesFlow completed. cause=$cause") }
                .collect()
        }
    }

    fun bindToState(snapshot: WearCredsSnapshot) {
        deviceSN = snapshot.selectedDeviceSN
        apiKey = snapshot.apiKey
        val lastUpdatedTime: LocalDateTime? = if (snapshot.lastRefreshTime == Instant.MIN) null else snapshot.lastRefreshTime.atZone(ZoneId.systemDefault()).toLocalDateTime()

        _state.value = _state.value.copy(
            solarAmount = snapshot.solarGenerationAmount,
            houseLoadAmount = snapshot.houseLoadAmount,
            batteryChargePower = snapshot.batteryChargeLevel,
            batterySOC = snapshot.batteryChargeAmount,
            gridAmount = snapshot.gridAmount,
            solarRangeDefinitions = snapshot.solarRangeDefinitions,
            totalExport = snapshot.totalExport,
            totalImport = snapshot.totalImport,
            lastUpdated = lastUpdatedTime
        )
    }

    suspend fun load() {
        if (_state.value.state.isActive()) {
            return
        }

        _state.value = _state.value.copy(state = LoadState.Active.Loading)

        val refresher = WearDataRefresher(
            context = application,
            store = store,
            scope = viewModelScope
        )

        when (val result = refresher.refresh()) {
            is WearDataRefreshResult.Success -> {
                _state.value = _state.value.copy(state = LoadState.Inactive)
                MainComplicationService.requestRefresh(application)
            }
            is WearDataRefreshResult.SkippedRecent -> {
                _state.value = _state.value.copy(state = LoadState.Inactive)
            }
            is WearDataRefreshResult.MissingCreds -> {
                _state.value = _state.value.copy(state = LoadState.Error(null, result.message))
            }
            is WearDataRefreshResult.Error -> {
                _state.value = _state.value.copy(
                    state = LoadState.Error(result.throwable, result.message)
                )
            }
        }
    }

    override fun onCleared() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(foregroundObserver)
        super.onCleared()
    }
}

class WearConfig(
    override var shouldInvertCT2: Boolean,
    override var shouldCombineCT2WithPVPower: Boolean,
    override var powerFlowStrings: PowerFlowStringsSettings,
    override var shouldCombineCT2WithLoadsPower: Boolean,
    override var allowNegativeLoad: Boolean,
    var showGridTotals: Boolean
) : CurrentStatusCalculatorConfig {
    override val themeStream: MutableStateFlow<AppTheme> = MutableStateFlow(AppTheme.demo())
}