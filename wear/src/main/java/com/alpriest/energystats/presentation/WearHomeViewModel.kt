package com.alpriest.energystats.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.config.CurrentStatusCalculatorConfig
import com.alpriest.energystats.shared.models.AppTheme
import com.alpriest.energystats.shared.models.BatteryViewModel
import com.alpriest.energystats.shared.models.DataCeiling
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.shared.models.PowerFlowStringsSettings
import com.alpriest.energystats.shared.models.QueryDate
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.alpriest.energystats.shared.models.TotalsViewModel
import com.alpriest.energystats.shared.models.demo
import com.alpriest.energystats.shared.models.isActive
import com.alpriest.energystats.shared.models.network.OpenReportResponse
import com.alpriest.energystats.shared.models.network.ReportType
import com.alpriest.energystats.shared.network.FoxAPIService
import com.alpriest.energystats.shared.network.NetworkCache
import com.alpriest.energystats.shared.network.NetworkFacade
import com.alpriest.energystats.shared.network.NetworkService
import com.alpriest.energystats.shared.network.NetworkValueCleaner
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.shared.network.RequestData
import com.alpriest.energystats.shared.services.CurrentStatusCalculator
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
        val deviceSN = store.selectedDeviceSN
        if (deviceSN.isNullOrEmpty() || store.apiKey.isNullOrEmpty()) {
            store.lastUpdatedTime = Instant.now().minusSeconds(10 * 60)
            _state.value = _state.value.copy(state = LoadState.Error(null, application.getString(R.string.no_device_api_key_found)))
            return
        }

        val now = Instant.now()
        if (store.lastUpdatedTime.isAfter(now.minusSeconds(4 * 60))) {
            return
        }

        if (_state.value.state.isActive()) {
            return
        }

        _state.value = _state.value.copy(state = LoadState.Active.Loading)

        val requestData = RequestData(
            apiKey = { store.apiKey ?: "" },
            userAgent = "Energy Stats Android WearOS"
        )
        val networking = NetworkService(
            NetworkValueCleaner(
                NetworkFacade(
                    api = NetworkCache(api = FoxAPIService(requestData)),
                    isDemoUser = { store.apiKey == "demo" }
                ),
                { DataCeiling.None }
            )
        )

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

        val config = WearConfig(
            store.shouldInvertCT2,
            store.shouldCombineCT2WithPVPower,
            PowerFlowStringsSettings.defaults,
            store.shouldCombineCT2WithLoadsPower,
            store.allowNegativeLoad,
            store.showGridTotals
        )
        val device = Device(deviceSN, true, null, "", true, "", null, "")
        val currentStatusCalculator = CurrentStatusCalculator(
            reals,
            device,
            config,
            viewModelScope
        )
        val values = currentStatusCalculator.currentValuesStream.value
        val batteryViewModel = BatteryViewModel.make(device, reals)
        val totals = loadTotals(config, networking, device)

        store.applyAndNotify {
            lastUpdatedTime = Instant.now()
            batteryChargeLevel = batteryViewModel.chargeLevel
            solarGenerationAmount = values.solarPower
            houseLoadAmount = values.homeConsumption
            gridAmount = values.grid
            batteryChargeAmount = batteryViewModel.chargePower
            totalExport = totals?.grid
            totalImport = totals?.loads
        }

        _state.value = _state.value.copy(state = LoadState.Inactive)
    }

    suspend fun loadTotals(config: WearConfig, networking: Networking, device: Device): TotalsViewModel? {
        if (!config.showGridTotals) {
            return null
        }

        return TotalsViewModel(reports = loadReportData(networking, device), generationViewModel = null)
    }

    private suspend fun loadReportData(networking: Networking, currentDevice: Device): List<OpenReportResponse> {
        val reportVariables = listOf(ReportVariable.FeedIn, ReportVariable.GridConsumption)

        return networking.fetchReport(
            deviceSN = currentDevice.deviceSN,
            variables = reportVariables,
            queryDate = QueryDate.invoke(),
            reportType = ReportType.month
        )
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