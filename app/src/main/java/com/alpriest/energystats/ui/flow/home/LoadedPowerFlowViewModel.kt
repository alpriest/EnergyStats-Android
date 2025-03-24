package com.alpriest.energystats.ui.flow.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.toUtcMillis
import com.alpriest.energystats.services.FoxServerError
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.BannerAlertManaging
import com.alpriest.energystats.ui.flow.EarningsViewModel
import com.alpriest.energystats.ui.flow.EnergyStatsFinancialModel
import com.alpriest.energystats.ui.flow.StringPower
import com.alpriest.energystats.ui.flow.TotalsViewModel
import com.alpriest.energystats.ui.flow.battery.BatteryPowerViewModel
import com.alpriest.energystats.ui.flow.currentData
import com.alpriest.energystats.ui.settings.TotalYieldModel
import com.alpriest.energystats.ui.settings.inverter.CT2DisplayMode
import com.alpriest.energystats.ui.statsgraph.ReportType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

const val dateFormat = "yyyy-MM-dd HH:mm:ss"

data class InverterTemperatures(
    val ambient: Double,
    val inverter: Double
)

class LoadedPowerFlowViewModel(
    val context: Context,
    val solar: Double,
    private val solarStrings: List<StringPower>,
    val home: Double,
    val grid: Double,
    val inverterTemperatures: InverterTemperatures?,
    val hasBattery: Boolean,
    val battery: BatteryViewModel,
    val configManager: ConfigManaging,
    val ct2: Double,
    val currentDevice: Device,
    val network: Networking,
    private val bannerAlertManager: BannerAlertManaging
) : ViewModel() {
    val deviceState = MutableStateFlow<DeviceState>(DeviceState.Unknown)
    val homeTotal = MutableStateFlow<Double?>(null)
    val gridImportTotal = MutableStateFlow<Double?>(null)
    val gridExportTotal = MutableStateFlow<Double?>(null)
    val earnings = MutableStateFlow<EarningsViewModel?>(null)
    val todaysGeneration = MutableStateFlow<GenerationViewModel?>(null)
    val faults = MutableStateFlow<List<String>>(listOf())
    val displayStrings = MutableStateFlow<List<StringPower>>(listOf())

    init {
        loadDeviceStatus()
        loadTotals()

        viewModelScope.launch {
            configManager.themeStream.collect { it ->
                displayStrings.value = listOf()

                if (it.ct2DisplayMode == CT2DisplayMode.AsPowerString) {
                    displayStrings.value = displayStrings.value.plus(StringPower("CT2", ct2))
                }

                if (it.powerFlowStrings.enabled) {
                    displayStrings.value = displayStrings.value.plus(solarStrings)
                }
            }
        }
    }

    private fun loadGeneration(solar: Double) {
        if (configManager.totalYieldModel == TotalYieldModel.Off) {
            return
        }

        viewModelScope.launch {
            try {
                val historyData: OpenHistoryResponse = if (configManager.ct2DisplayMode == CT2DisplayMode.SeparateIcon ||
                    configManager.ct2DisplayMode == CT2DisplayMode.AsPowerString ||
                    configManager.shouldCombineCT2WithPVPower) {
                    loadHistoryData(currentDevice)
                } else {
                    OpenHistoryResponse(deviceSN = currentDevice.deviceSN, listOf())
                }

                val model = GenerationViewModel(
                    solar,
                    historyData,
                    includeCT2 = configManager.shouldCombineCT2WithPVPower,
                    invertCT2 = configManager.shouldInvertCT2
                )

                todaysGeneration.value = model
            } catch (ex: FoxServerError) {
                bannerAlertManager.showToast("Failed to load generation: ${ex.message}")
            }
        }
    }

    private suspend fun loadHistoryData(device: Device): OpenHistoryResponse {
        val start = QueryDate().toUtcMillis()
        return network.fetchHistory(
            deviceSN = device.deviceSN,
            variables = listOf("meterPower2"),
            start = start,
            end = start + (86400 * 1000)
        )
    }

    private fun loadDeviceStatus() {
        viewModelScope.launch {
            try {
                deviceState.value = loadDeviceStatus(currentDevice)

                if (deviceState.value != DeviceState.Online) {
                    val response = network.fetchRealData(currentDevice.deviceSN, variables = listOf("currentFault"))

                    faults.value = response.datas.currentData("currentFault")?.valueString?.let {
                        return@let it.split(",").filter { it.isNotBlank() }
                    } ?: listOf()

                    if (deviceState.value == DeviceState.Offline) {
                        bannerAlertManager.deviceIsOffline()
                    } else {
                        bannerAlertManager.clearDeviceBanner()
                    }
                } else {
                    bannerAlertManager.clearDeviceBanner()
                }
            } catch (ex: FoxServerError) {
                bannerAlertManager.showToast("Failed to load device status: ${ex.message}")
            }
        }
    }

    private suspend fun loadDeviceStatus(currentDevice: Device): DeviceState {
        val device = network.fetchDevice(currentDevice.deviceSN)
        return DeviceState.fromInt(device.status)
    }

    private fun loadTotals() {
        if (configManager.showHomeTotal || configManager.showGridTotals || configManager.showFinancialSummary || configManager.totalYieldModel != TotalYieldModel.Off) {
            viewModelScope.launch {
                try {
                    val totals = TotalsViewModel(loadReportData(currentDevice))
                    earnings.value = EarningsViewModel(EnergyStatsFinancialModel(totals, configManager))
                    homeTotal.value = totals.loads
                    gridImportTotal.value = totals.grid
                    gridExportTotal.value = totals.feedIn

                    loadGeneration(totals.solar)
                } catch (ex: FoxServerError) {
                    bannerAlertManager.showToast("Failed to load totals: ${ex.message}")
                }
            }
        }
    }

    private suspend fun loadReportData(currentDevice: Device): List<OpenReportResponse> {
        var reportVariables = listOf(ReportVariable.Loads, ReportVariable.FeedIn, ReportVariable.GridConsumption, ReportVariable.PvEnergyToTal)
        if (currentDevice.hasBattery) {
            reportVariables = reportVariables.plus(listOf(ReportVariable.ChargeEnergyToTal, ReportVariable.DischargeEnergyToTal))
        }

        return network.fetchReport(
            currentDevice.deviceSN,
            reportVariables,
            QueryDate(),
            ReportType.month
        )
    }

    val batteryViewModel: BatteryPowerViewModel? = if (hasBattery)
        BatteryPowerViewModel(configManager, battery.chargeLevel, battery.chargePower, battery.temperatures, battery.residual)
    else
        null
}
