package com.alpriest.energystats.ui.flow

import android.os.CountDownTimer
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.RawDataStoring
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.home.SummaryPowerFlowViewModel
import com.alpriest.energystats.ui.settings.RefreshFrequency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

data class UiUpdateMessageState(
    val updateState: UpdateMessageState
)

sealed class UpdateMessageState {
    @Composable
    abstract fun toString2(): String
}

object LoadingNowUpdateMessageState : UpdateMessageState() {
    @Composable
    override fun toString2(): String {
        return stringResource(R.string.loading)
    }
}

class PendingUpdateMessageState(private val nextUpdateSeconds: Int) : UpdateMessageState() {
    @Composable
    override fun toString2(): String {
        return String.format(stringResource(R.string.nextUpdate, nextUpdateSeconds))
    }
}

object EmptyUpdateMessageState: UpdateMessageState() {
    @Composable
    override fun toString2(): String {
        return " "
    }
}

class PowerFlowTabViewModel(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val rawDataStore: RawDataStoring
) : ViewModel() {
    private var timer: CountDownTimer? = null

    private val _uiState = MutableStateFlow(UiLoadState(LoadingLoadState))
    val uiState: StateFlow<UiLoadState> = _uiState.asStateFlow()

    private val _updateMessage: MutableStateFlow<UiUpdateMessageState> = MutableStateFlow(UiUpdateMessageState(EmptyUpdateMessageState))
    val updateMessage: StateFlow<UiUpdateMessageState> = _updateMessage.asStateFlow()

    private var isLoading = false
    private var totalSeconds = 60

    fun timerFired() {
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
        timer = object : CountDownTimer(totalSeconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds: Int = (millisUntilFinished / 1000).toInt()
                _updateMessage.value = UiUpdateMessageState(PendingUpdateMessageState(seconds))
            }

            override fun onFinish() {
                timerFired()
            }
        }.start()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _updateMessage.value = UiUpdateMessageState(LoadingNowUpdateMessageState)
                if (_uiState.value.state is ErrorLoadState) {
                    _uiState.value = UiLoadState(LoadingLoadState)
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
                    batteryTemperature = battery.temperature,
                    raw = raw
                )
                _uiState.value = UiLoadState(LoadedLoadState(summary))
                _updateMessage.value = UiUpdateMessageState(EmptyUpdateMessageState)
                calculateTicks(summary)
            } catch (ex: Exception) {
                stopTimer()
                _uiState.value = UiLoadState(ErrorLoadState(ex.localizedMessage ?: "Error unknown"))
                _updateMessage.value = UiUpdateMessageState(EmptyUpdateMessageState)
            }
        }
    }

    private fun calculateTicks(summary: SummaryPowerFlowViewModel) {
        totalSeconds = when (configManager.refreshFrequency) {
            RefreshFrequency.OneMinute -> 60
            RefreshFrequency.FiveMinutes -> 300
            RefreshFrequency.Auto -> 60
        }
    }
}