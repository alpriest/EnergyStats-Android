package com.alpriest.energystats.ui.flow

import android.os.CountDownTimer
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.pluralStringResource
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

interface IPowerFlowTabViewModel {
    val uiState: StateFlow<UiLoadState>
    val updateMessage: StateFlow<UiUpdateState?>
    val lastUpdateDate: StateFlow<Date?>
    fun timerFired()
}

data class UiUpdateState(
    val updateState: UpdateState
)

sealed class UpdateState {
    @Composable
    abstract fun toString2(): String
}

object LoadingNowUpdateState : UpdateState() {
    @Composable
    override fun toString2(): String {
        return stringResource(R.string.loading)
    }
}

class PendingUpdateState(private val nextUpdateSeconds: Int) : UpdateState() {
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun toString2(): String {
        return pluralStringResource(
            R.plurals.nextUpdate,
            nextUpdateSeconds,
            nextUpdateSeconds
        )
    }
}

class PowerFlowTabViewModel(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val rawDataStore: RawDataStoring
) : ViewModel(), IPowerFlowTabViewModel {
    private var timer: CountDownTimer? = null

    private val _uiState = MutableStateFlow(UiLoadState(LoadingLoadState))
    override val uiState: StateFlow<UiLoadState> = _uiState.asStateFlow()

    private val _updateMessage: MutableStateFlow<UiUpdateState?> = MutableStateFlow(null)
    override val updateMessage: StateFlow<UiUpdateState?> = _updateMessage.asStateFlow()

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
                _updateMessage.value = UiUpdateState(PendingUpdateState(seconds))
//                _updateMessage.value = "Next update in ${seconds}s"
            }

            override fun onFinish() {
                timerFired()
            }
        }.start()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _updateMessage.value = UiUpdateState(LoadingNowUpdateState)
//                _updateMessage.value = "Loading..."
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
                    raw = raw
                )
                _uiState.value = UiLoadState(LoadedLoadState(summary))
                _updateMessage.value = null
            } catch (ex: Exception) {
                stopTimer()
                _uiState.value = UiLoadState(ErrorLoadState(ex.localizedMessage ?: "Error unknown"))
                _updateMessage.value = null
            }
        }
    }
}