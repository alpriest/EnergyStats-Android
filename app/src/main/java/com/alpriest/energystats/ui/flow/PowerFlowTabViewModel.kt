package com.alpriest.energystats.ui.flow

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.RawDataStoring
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.home.SummaryPowerFlowViewModel
import com.alpriest.energystats.ui.flow.powerflowstate.EmptyUpdateMessageState
import com.alpriest.energystats.ui.flow.powerflowstate.LoadingNowUpdateMessageState
import com.alpriest.energystats.ui.flow.powerflowstate.PendingUpdateMessageState
import com.alpriest.energystats.ui.flow.powerflowstate.UiUpdateMessageState
import com.alpriest.energystats.ui.settings.RefreshFrequency
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.locks.ReentrantLock

class PowerFlowTabViewModel(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val rawDataStore: RawDataStoring
) : ViewModel() {

    var launchIn: Job? = null

    private var timer: CountDownTimer? = null

    private val _uiState = MutableStateFlow(UiLoadState(LoadingLoadState))
    val uiState: StateFlow<UiLoadState> = _uiState.asStateFlow()

    private val _updateMessage: MutableStateFlow<UiUpdateMessageState> = MutableStateFlow(UiUpdateMessageState(EmptyUpdateMessageState))
    val updateMessage: StateFlow<UiUpdateMessageState> = _updateMessage.asStateFlow()

    private val appLifecycleObserver = AppLifecycleObserver(
        onAppGoesToBackground = { timer?.cancel() },
        onAppEntersForeground = { timerFired() }
    )

    private var isLoading = false
    private var totalSeconds = 60
    private val lock = ReentrantLock()

    init {
        appLifecycleObserver.attach()
    }

    fun finalize() {
        appLifecycleObserver.detach()
    }

    fun timerFired() {
        lock.run {
            if (isLoading) {
                return
            }

            isLoading = true

            viewModelScope.launch {
                try {
                    loadData()
                    startTimer()
                } finally {
                    isLoading = false
                }
            }

            if (launchIn == null) {
                launchIn = configManager.currentDevice
                    .onEach {
                        if (it != null) {
                            viewModelScope.launch {
                                timerFired()
                            }
                        }
                    }.launchIn(viewModelScope)
            }
        }
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    private fun startTimer() {
        stopTimer()
        timer = object : CountDownTimer(totalSeconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds: Int = (millisUntilFinished / 1000).toInt()
                _updateMessage.value = UiUpdateMessageState(PendingUpdateMessageState(seconds))
            }

            override fun onFinish() {
                timerFired()
            }
        }
        timer?.start()
    }

    private suspend fun loadData() {
        try {
            if (configManager.currentDevice.value == null) {
                configManager.fetchDevices()
            }

            configManager.currentDevice.value?.let { currentDevice ->
                _updateMessage.value = UiUpdateMessageState(LoadingNowUpdateMessageState)
                if (_uiState.value.state is ErrorLoadState) {
                    _uiState.value = UiLoadState(LoadingLoadState)
                }
                network.ensureHasToken()

                val earnings = network.fetchEarnings(deviceID = currentDevice.deviceID)

                val variables: List<RawVariable> = listOfNotNull(
                    variable("feedInPower"),
                    variable("gridConsumptionPower"),
                    variable("generationPower"),
                    variable("loadsPower"),
                    variable("batChargePower"),
                    variable("batDischargePower")
                )
                val raw = network.fetchRaw(
                    deviceID = currentDevice.deviceID,
                    variables,
                    QueryDate()
                )
                rawDataStore.store(raw = raw)

                val battery: BatteryViewModel = if (currentDevice.battery != null) {
                    val battery = network.fetchBattery(deviceID = currentDevice.deviceID)
                    rawDataStore.store(battery = battery)
                    BatteryViewModel(battery)
                } else {
                    BatteryViewModel.noBattery()
                }

                val summary = SummaryPowerFlowViewModel(
                    configManager = configManager,
                    battery = battery.chargePower,
                    batteryStateOfCharge = battery.chargeLevel,
                    raw = raw,
                    batteryTemperature = battery.temperature,
                    todaysGeneration = earnings.today.generation
                )
                _uiState.value = UiLoadState(LoadedLoadState(summary))
                _updateMessage.value = UiUpdateMessageState(EmptyUpdateMessageState)
                calculateTicks(summary)
            }
        } catch (ex: Exception) {
            stopTimer()
            _uiState.value = UiLoadState(ErrorLoadState(ex.localizedMessage ?: "Error unknown"))
            _updateMessage.value = UiUpdateMessageState(EmptyUpdateMessageState)
        }
    }

    private fun variable(variableName: String): RawVariable? {
        return configManager.variables.firstOrNull { it.variable.lowercase() == variableName.lowercase() }
    }

    private fun calculateTicks(summary: SummaryPowerFlowViewModel) {
        val diff = (Date().time - summary.latestUpdate.time) / 1000

        totalSeconds = when (configManager.refreshFrequency) {
            RefreshFrequency.OneMinute -> 60
            RefreshFrequency.FiveMinutes -> 300
            RefreshFrequency.Auto -> (300 - diff + 10).toInt()
        }
    }
}