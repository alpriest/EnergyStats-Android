package com.alpriest.energystats.ui.flow

import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.models.rounded
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.home.GenerationViewModel
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Currency
import java.util.Locale
import java.util.concurrent.locks.ReentrantLock

class PowerFlowTabViewModel(
    private val network: FoxESSNetworking,
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

                val earnings = network.fetchEarnings(deviceID = currentDevice.deviceSN)
                configManager.currencyCode = earnings.currencyCode()
                configManager.currencySymbol = earnings.currencySymbol()

                var variables: List<Variable> = listOfNotNull(
                    variable("feedInPower"),
                    variable("gridConsumptionPower"),
                    variable("generationPower"),
                    variable("loadsPower"),
                    variable("pvPower"),
                    variable("meterPower2")
                )

                if (configManager.showInverterTemperatures) {
                    variables = variables.plus(
                        listOfNotNull(
                            variable("ambientTemperation"),
                            variable("invTemperation")
                        )
                    )
                }

                var reportVariables = listOf(ReportVariable.Loads, ReportVariable.FeedIn, ReportVariable.GridConsumption)
//                if (currentDevice.hasBattery) {
//                    reportVariables = reportVariables.plus(listOf(ReportVariable.ChargeEnergyToTal, ReportVariable.DischargeEnergyToTal))
//                }

                val report = network.fetchReport(
                    currentDevice.deviceSN,
                    reportVariables,
                    QueryDate(),
                    ReportType.month
                )

                val real = network.openapi_fetchRealData(
                    deviceSN = currentDevice.deviceSN,
                    variables
                )

                val currentValues = RealQueryResponseMapper().mapCurrentValues(real)
                val currentViewModel = CurrentStatusCalculator(currentValues, configManager.shouldInvertCT2, configManager.shouldCombineCT2WithPVPower)
                val totals = TotalsViewModel(report)

//                val battery: BatteryViewModel = if (currentDevice.battery != null || currentDevice.hasBattery) {
//                    val battery = network.fetchBattery(deviceID = currentDevice.deviceSN)
//                    BatteryViewModel(battery, hasError = currentDevice.battery?.hasError ?: false)
//                } else {
                val battery = BatteryViewModel.noBattery()
//                }

                val start = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond()
                val history = network.openapi_fetchHistory(
                    deviceSN = currentDevice.deviceSN,
                    variables = listOf("pvPower", "meterPower2"),
                    start = start,
                    end = start.and(86400)
                )

                val summary = HomePowerFlowViewModel(
                    solar = currentViewModel.currentSolarPower,
                    home = currentViewModel.currentHomeConsumption,
                    grid = currentViewModel.currentGrid,
                    todaysGeneration = GenerationViewModel(history),
                    earnings = EarningsViewModel(earnings, EnergyStatsFinancialModel(totals, configManager)),
                    inverterTemperatures = currentViewModel.currentTemperatures,
                    hasBattery = battery.hasBattery,
                    battery = battery,
                    configManager = configManager,
                    homeTotal = totals.loads,
                    gridImportTotal = totals.grid,
                    gridExportTotal = totals.feedIn,
                    ct2 = currentViewModel.currentCT2
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

    private fun variable(variableName: String): Variable? {
        return configManager.variables.firstOrNull { it.variable.lowercase() == variableName.lowercase() }
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

fun Double.roundedToString(decimalPlaces: Int, currencyCode: String? = null, currencySymbol: String? = null): String {
    val roundedNumber = this.rounded(decimalPlaces)

    try {
        return if (currencyCode != null) {
            val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
            numberFormat.currency = Currency.getInstance(currencyCode)
            numberFormat.maximumFractionDigits = decimalPlaces
            numberFormat.minimumFractionDigits = decimalPlaces

            numberFormat.format(roundedNumber)
        } else {
            val numberFormat = NumberFormat.getNumberInstance()
            numberFormat.maximumFractionDigits = decimalPlaces
            numberFormat.minimumFractionDigits = decimalPlaces

            numberFormat.format(roundedNumber)
        }
    } catch (ex: Exception) {
        return "$currencySymbol$roundedNumber.toString()"
    }
}
