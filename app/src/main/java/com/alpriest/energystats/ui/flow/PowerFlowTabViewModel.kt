package com.alpriest.energystats.ui.flow

import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.OpenQueryResponse
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.rounded
import com.alpriest.energystats.models.toUtcMillis
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.home.DeviceState
import com.alpriest.energystats.ui.flow.home.GenerationViewModel
import com.alpriest.energystats.ui.flow.home.LoadedPowerFlowViewModel
import com.alpriest.energystats.ui.flow.powerflowstate.EmptyUpdateMessageState
import com.alpriest.energystats.ui.flow.powerflowstate.LoadingNowUpdateMessageState
import com.alpriest.energystats.ui.flow.powerflowstate.PendingUpdateMessageState
import com.alpriest.energystats.ui.flow.powerflowstate.UiUpdateMessageState
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.ui.statsgraph.ReportType
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.locks.ReentrantLock

class PowerFlowTabViewModel(
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val themeStream: MutableStateFlow<AppTheme>,
    private val context: Context
) : ViewModel() {

    private var launchIn: Job? = null
    private var timer: CountDownTimer? = null

    val uiState = MutableStateFlow(UiPowerFlowLoadState(PowerFlowLoadState.Active("")))
    val updateMessage: MutableStateFlow<UiUpdateMessageState> = MutableStateFlow(UiUpdateMessageState(EmptyUpdateMessageState))
    val batteryErrorStream = MutableStateFlow(false)

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
                if (it.showInverterTemperatures || it.shouldInvertCT2) {
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
                    }
                    .launchIn(viewModelScope)
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
                updateMessage.value = UiUpdateMessageState(PendingUpdateMessageState(seconds, lastUpdateTime))
            }

            override fun onFinish() {
                timerFired()
            }
        }
        timer?.start()
    }

    private suspend fun loadRealData(device: Device, configManager: ConfigManaging): OpenQueryResponse {
        val variables: MutableList<String> = mutableListOf(
            "feedinPower",
            "gridConsumptionPower",
            "loadsPower",
            "generationPower",
            "pvPower",
            "meterPower2",
            "ambientTemperation",
            "invTemperation",
            "batChargePower",
            "batDischargePower",
            "SoC",
            "batTemperature",
            "ResidualEnergy",
            "epsPower"
        )

        if (configManager.powerFlowStrings.enabled) {
            variables.addAll(configManager.powerFlowStrings.variableNames())
        }

        return network.fetchRealData(
            deviceSN = device.deviceSN,
            variables
        )
    }

    private suspend fun loadReportData(device: Device): List<OpenReportResponse> {
        var reportVariables = listOf(ReportVariable.Loads, ReportVariable.FeedIn, ReportVariable.GridConsumption)
        if (device.hasBattery) {
            reportVariables = reportVariables.plus(listOf(ReportVariable.ChargeEnergyToTal, ReportVariable.DischargeEnergyToTal))
        }

        return network.fetchReport(
            device.deviceSN,
            reportVariables,
            QueryDate(),
            ReportType.month
        )
    }

    private suspend fun loadTotals(device: Device): TotalsViewModel {
        return TotalsViewModel(loadReportData(device))
    }

    private suspend fun loadGeneration(device: Device): GenerationViewModel {
        return GenerationViewModel(loadHistoryData(device), includeCT2 = configManager.shouldCombineCT2WithPVPower, invertCT2 = configManager.shouldInvertCT2)
    }

    private suspend fun loadHistoryData(device: Device): OpenHistoryResponse {
        val start = QueryDate().toUtcMillis()
        return network.fetchHistory(
            deviceSN = device.deviceSN,
            variables = listOf("pvPower", "meterPower2"),
            start = start,
            end = start + (86400 * 1000)
        )
    }

    private suspend fun loadDeviceStatus(currentDevice: Device): DeviceState {
        val device = network.fetchDevice(currentDevice.deviceSN)
        return DeviceState.fromInt(device.status)
    }

    private suspend fun loadData() {
        try {
            if (configManager.currentDevice.value == null) {
                configManager.fetchDevices()
            }

            configManager.currentDevice.value?.let { currentDevice ->
                updateMessage.value = UiUpdateMessageState(LoadingNowUpdateMessageState)
                if (uiState.value.state is PowerFlowLoadState.Error) {
                    uiState.value = UiPowerFlowLoadState(PowerFlowLoadState.Active(context.getString(R.string.loading)))
                }

                val deviceState = loadDeviceStatus(currentDevice)
                val real = loadRealData(currentDevice, configManager)
                val totals = loadTotals(currentDevice)
                val generation = loadGeneration(currentDevice)

                val currentViewModel = CurrentStatusCalculator(
                    real,
                    currentDevice.hasPV,
                    configManager
                )

                val battery: BatteryViewModel = BatteryViewModel.make(currentDevice, real)

                val summary = LoadedPowerFlowViewModel(
                    solar = currentViewModel.currentSolarPower,
                    solarStrings = currentViewModel.currentSolarStringsPower,
                    home = currentViewModel.currentHomeConsumption,
                    grid = currentViewModel.currentGrid,
                    todaysGeneration = generation,
                    earnings = EarningsViewModel(EnergyStatsFinancialModel(totals, configManager)),
                    inverterTemperatures = currentViewModel.currentTemperatures,
                    hasBattery = battery.hasBattery,
                    battery = battery,
                    configManager = configManager,
                    homeTotal = totals.loads,
                    gridImportTotal = totals.grid,
                    gridExportTotal = totals.feedIn,
                    ct2 = currentViewModel.currentCT2,
                    deviceState = deviceState
                )
                batteryErrorStream.value = currentDevice.battery?.hasError ?: false
                uiState.value = UiPowerFlowLoadState(PowerFlowLoadState.Loaded(summary))
                updateMessage.value = UiUpdateMessageState(EmptyUpdateMessageState)
                lastUpdateTime = currentViewModel.lastUpdate
                calculateTicks(currentViewModel)
            }
        } catch (ex: Exception) {
            stopTimer()
            uiState.value = UiPowerFlowLoadState(PowerFlowLoadState.Error(ex, ex.localizedMessage ?: "Error unknown"))
            updateMessage.value = UiUpdateMessageState(EmptyUpdateMessageState)
        }
    }

    private fun calculateTicks(summary: CurrentStatusCalculator) {
        val diff = kotlin.math.abs(Duration.between(LocalDateTime.now(), summary.lastUpdate).seconds)

        totalSeconds = when (configManager.refreshFrequency) {
            RefreshFrequency.OneMinute -> 60
            RefreshFrequency.FiveMinutes -> 300
            RefreshFrequency.Auto -> {
                var newTicks = (300 - diff + 10).toInt()
                if (newTicks <= 0) {
                    newTicks = 300
                }

                newTicks
            }
        }
    }
}

fun Double.roundedToString(decimalPlaces: Int, currencySymbol: String = ""): String {
    val roundedNumber = this.rounded(decimalPlaces)

    val numberFormat = NumberFormat.getNumberInstance()
    numberFormat.maximumFractionDigits = decimalPlaces
    numberFormat.minimumFractionDigits = decimalPlaces

    val formattedNumber = numberFormat.format(roundedNumber)

    return "$currencySymbol$formattedNumber"
}
