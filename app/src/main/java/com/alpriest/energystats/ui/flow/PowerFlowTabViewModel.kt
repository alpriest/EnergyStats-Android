package com.alpriest.energystats.ui.flow

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.RawDataStoring
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.home.SummaryPowerFlowViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

interface IPowerFlowTabViewModel {
    val uiState: StateFlow<UiState>
    val updateMessage: StateFlow<String?>
    val lastUpdateDate: StateFlow<Date?>
    fun timerFired()
}

class PowerFlowTabViewModel(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val rawDataStore: RawDataStoring
) : ViewModel(), IPowerFlowTabViewModel {
    private var timer: CountDownTimer? = null

    private val _uiState = MutableStateFlow(UiState(Loading))
    override val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _updateMessage: MutableStateFlow<String?> = MutableStateFlow(null)
    override val updateMessage: StateFlow<String?> = _updateMessage.asStateFlow()

    private val _lastUpdateDate: MutableStateFlow<Date?> = MutableStateFlow(null)
    override val lastUpdateDate: StateFlow<Date?> = _lastUpdateDate.asStateFlow()

    private var isLoading = false

    override fun timerFired() {
        if (isLoading) {
            return
        }

        isLoading = true

        try {
            loadData()
            startTimer()
        } finally {
            isLoading = false
        }
    }

    private fun stopTimer() {
        timer?.cancel()
    }

    private fun startTimer() {
        stopTimer()
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds: Int = (millisUntilFinished / 1000).toInt()
                _updateMessage.value = "Next update in ${seconds}s"
            }

            override fun onFinish() {
                timerFired()
            }
        }.start()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _updateMessage.value = "Loading..."
                if (_uiState.value.loadState is Error) {
                    _uiState.value = UiState(Loading)
                }
                network.ensureHasToken()
                val raw = network.fetchRaw(
                    arrayOf(
                        RawVariable.FeedInPower,
                        RawVariable.GridConsumptionPower,
                        RawVariable.GenerationPower,
                        RawVariable.LoadsPower,
                        RawVariable.BatChargePower,
                        RawVariable.BatDischargePower
                    )
                )
                rawDataStore.store(raw = raw)

                val battery: BatteryViewModel = if (configManager.hasBattery) {
                    val battery = network.fetchBattery()
                    rawDataStore.store(battery = battery)
                    BatteryViewModel(battery)
                } else {
                    BatteryViewModel.noBattery()
                }

                val summary = SummaryPowerFlowViewModel(
                    configManager = configManager,
                    battery = battery.chargePower,
                    batteryStateOfCharge = battery.chargeLevel,
                    hasBattery = battery.hasBattery,
                    raw = raw
                )
                _uiState.value = UiState(Loaded(summary))
                _updateMessage.value = null
            } catch (ex: Exception) {
                stopTimer()
                _uiState.value = UiState(Error("Failed: " + ex.localizedMessage))
                _updateMessage.value = null
            }
        }
    }
}