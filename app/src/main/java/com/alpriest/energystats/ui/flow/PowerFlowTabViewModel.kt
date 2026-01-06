package com.alpriest.energystats.ui.flow

import android.app.Application
import android.os.CountDownTimer
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.WatchSyncManager
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.shared.helpers.truncated
import com.alpriest.energystats.shared.models.BatteryData
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.shared.models.network.OpenRealQueryResponse
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.stores.WidgetDataSharing
import com.alpriest.energystats.ui.flow.home.LoadedPowerFlowViewModel
import com.alpriest.energystats.ui.flow.powerflowstate.EmptyUpdateMessageState
import com.alpriest.energystats.ui.flow.powerflowstate.LoadingNowUpdateMessageState
import com.alpriest.energystats.ui.flow.powerflowstate.PendingUpdateMessageState
import com.alpriest.energystats.ui.flow.powerflowstate.UiUpdateMessageState
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.shared.models.AppTheme
import com.alpriest.energystats.widget.BatteryWidget
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale
import java.util.concurrent.CancellationException
import java.util.concurrent.locks.ReentrantLock

class PowerFlowTabViewModel(
    private val application: Application,
    private val network: Networking,
    private val configManager: ConfigManaging,
    private val themeStream: MutableStateFlow<AppTheme>,
    private val widgetDataSharer: WidgetDataSharing,
    private val bannerAlertManager: BannerAlertManaging
) : AndroidViewModel(application) {

    private var currentViewModel: CurrentStatusCalculator? = null
    val bannerAlertStream: MutableStateFlow<BannerAlertType?> = bannerAlertManager.bannerAlertStream
    private var launchIn: Job? = null
    private var timer: CountDownTimer? = null

    val uiState = MutableStateFlow(UiPowerFlowLoadState(PowerFlowLoadState.Active("")))
    val updateMessage: MutableStateFlow<UiUpdateMessageState> = MutableStateFlow(UiUpdateMessageState(EmptyUpdateMessageState))

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
        timer?.cancel()
        timer = null
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

    private suspend fun loadRealData(device: Device, configManager: ConfigManaging): OpenRealQueryResponse {
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
            "SoC_1",
            "batTemperature",
            "batTemperature_1",
            "batTemperature_2",
            "ResidualEnergy"
        )

        if (configManager.powerFlowStrings.enabled) {
            variables.addAll(configManager.powerFlowStrings.variableNames())
        }

        return network.fetchRealData(
            deviceSN = device.deviceSN,
            variables
        )
    }

    private suspend fun loadData() {
        try {
            if (configManager.currentDevice.value == null) {
                configManager.fetchDevices()
            }

            configManager.currentDevice.value?.let { currentDevice ->
                updateMessage.value = UiUpdateMessageState(LoadingNowUpdateMessageState)
                if (uiState.value.state is PowerFlowLoadState.Error) {
                    uiState.value = UiPowerFlowLoadState(PowerFlowLoadState.Active(application.getString(R.string.loading)))
                }

                val real = loadRealData(currentDevice, configManager)

                val currentViewModel = CurrentStatusCalculator(
                    real,
                    currentDevice,
                    configManager,
                    viewModelScope
                )
                this.currentViewModel = currentViewModel

                val battery: BatteryViewModel = BatteryViewModel.make(currentDevice, real, configManager, application)
                if (battery.hasBattery) {
                    viewModelScope.launch {
                        try {
                            val batterySettings = network.fetchBatterySettings(currentDevice.deviceSN)
                            configManager.minSOC = batterySettings.minSocOnGridPercent()
                        } catch (_: Exception) {
                            // Ignore exceptions which can occur if the device is offline
                        }
                    }
                }
                widgetDataSharer.batteryData = BatteryData(chargeDescription = battery.chargeDescription, battery.chargeLevel)
                BatteryWidget().updateAll(application)

                val summary = LoadedPowerFlowViewModel(
                    application,
                    currentValuesStream = currentViewModel.currentValuesStream,
                    hasBattery = battery.hasBattery,
                    battery = battery,
                    configManager = configManager,
                    currentDevice = currentDevice,
                    network = network,
                    bannerAlertManager
                )
                WatchSyncManager().sendWatchStatsData(application, currentViewModel.currentValuesStream, battery)
                uiState.value = UiPowerFlowLoadState(PowerFlowLoadState.Loaded(summary))
                updateMessage.value = UiUpdateMessageState(EmptyUpdateMessageState)
                lastUpdateTime = currentViewModel.lastUpdate
                calculateTicks(currentViewModel)
            }
        } catch (ex: CancellationException) {
            // Ignore as the user navigated away
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

fun Double.roundedToString(decimalPlaces: Int, currencySymbol: String = "", locale: Locale = Locale.getDefault()): String {
    val roundedNumber = this.truncated(decimalPlaces)

    val numberFormat = NumberFormat.getNumberInstance(locale)
    numberFormat.maximumFractionDigits = decimalPlaces
    numberFormat.minimumFractionDigits = decimalPlaces

    val formattedNumber = numberFormat.format(roundedNumber)

    return "$currencySymbol$formattedNumber"
}
