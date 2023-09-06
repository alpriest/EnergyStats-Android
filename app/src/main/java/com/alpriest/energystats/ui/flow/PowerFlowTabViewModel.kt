package com.alpriest.energystats.ui.flow

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.EarningsResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.rounded
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.home.HomePowerFlowViewModel
import com.alpriest.energystats.ui.flow.powerflowstate.EmptyUpdateMessageState
import com.alpriest.energystats.ui.flow.powerflowstate.LoadingNowUpdateMessageState
import com.alpriest.energystats.ui.flow.powerflowstate.PendingUpdateMessageState
import com.alpriest.energystats.ui.flow.powerflowstate.UiUpdateMessageState
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.ui.statsgraph.ReportType
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDateTime
import java.util.Currency
import java.util.Locale
import java.util.concurrent.locks.ReentrantLock

class PowerFlowTabViewModel(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val themeStream: MutableStateFlow<AppTheme>
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
    private var lastUpdateTime = LocalDateTime.now()

    init {
        appLifecycleObserver.attach()

        viewModelScope.launch {
            themeStream.collect { it ->
                if (it.showInverterTemperatures) {
                    timerFired()
                }
            }
        }
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
                    lastUpdateTime = LocalDateTime.now()
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
                _updateMessage.value = UiUpdateMessageState(PendingUpdateMessageState(seconds, lastUpdateTime))
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

                var variables: List<RawVariable> = listOfNotNull(
                    variable("feedInPower"),
                    variable("gridConsumptionPower"),
                    variable("generationPower"),
                    variable("loadsPower"),
                    variable("pvPower")
                )

                if (configManager.showInverterTemperatures) {
                    variables = variables.plus(
                        listOfNotNull(
                            variable("ambientTemperation"),
                            variable("invTemperation")
                        )
                    )
                }

                val raws = network.fetchRaw(
                    deviceID = currentDevice.deviceID,
                    variables,
                    QueryDate()
                )

                val report = network.fetchReport(
                    currentDevice.deviceID,
                    listOf(ReportVariable.Loads, ReportVariable.FeedIn, ReportVariable.GridConsumption),
                    QueryDate(),
                    ReportType.month
                )

                val currentViewModel = CurrentStatusViewModel(currentDevice, raws, configManager.shouldInvertCT2)
                val totals = TotalsViewModel(report)

                val battery: BatteryViewModel = if (currentDevice.battery != null || currentDevice.hasBattery) {
                    val battery = network.fetchBattery(deviceID = currentDevice.deviceID)
                    BatteryViewModel(battery)
                } else {
                    BatteryViewModel.noBattery()
                }

                val summary = HomePowerFlowViewModel(
                    solar = currentViewModel.currentSolarPower,
                    home = currentViewModel.currentHomeConsumption,
                    grid = currentViewModel.currentGrid,
                    todaysGeneration = earnings.today.generation,
                    earnings = makeEarnings(earnings),
                    inverterTemperatures = currentViewModel.inverterTemperatures,
                    hasBattery = battery.hasBattery,
                    battery = battery,
                    configManager = configManager,
                    homeTotal = totals.homeTotal,
                    gridImportTotal = totals.gridImportTotal,
                    gridExportTotal = totals.gridExportTotal
                )
                _uiState.value = UiLoadState(LoadedLoadState(summary))
                _updateMessage.value = UiUpdateMessageState(EmptyUpdateMessageState)
                calculateTicks(currentViewModel)
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

    private fun calculateTicks(summary: CurrentStatusViewModel) {
        val diff = kotlin.math.abs(Duration.between(LocalDateTime.now(), summary.lastUpdate).seconds)

        totalSeconds = when (configManager.refreshFrequency) {
            RefreshFrequency.OneMinute -> 60
            RefreshFrequency.FiveMinutes -> 300
            RefreshFrequency.Auto -> (300 - diff + 10).toInt()
        }
    }

    private fun makeEarnings(response: EarningsResponse): String {
        return listOf(
            response.today.earnings.roundedToString(2, response.currencyCode(), response.currencySymbol()),
            response.month.earnings.roundedToString(2, response.currencyCode(), response.currencySymbol()),
            response.year.earnings.roundedToString(2, response.currencyCode(), response.currencySymbol()),
            response.cumulate.earnings.roundedToString(2, response.currencyCode(), response.currencySymbol())
        ).joinToString(separator = " ⋅ ")
    }
}

fun Double.roundedToString(decimalPlaces: Int, currencyCode: String, currencySymbol: String): String {
    val roundedNumber = this.rounded(decimalPlaces)

    return try {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        numberFormat.currency = Currency.getInstance(currencyCode)
        numberFormat.maximumFractionDigits = decimalPlaces

        numberFormat.format(roundedNumber)
    } catch (ex: Exception) {
        currencySymbol + roundedNumber.toString()
    }
}
