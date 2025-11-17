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
import com.alpriest.energystats.ui.flow.CurrentValues
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

const val dateFormat = "yyyy-MM-dd HH:mm:ss"

data class InverterTemperatures(
    val ambient: Double,
    val inverter: Double
)

class LoadedPowerFlowViewModel(
    val context: Context,
    currentValuesStream: StateFlow<CurrentValues>,
    val hasBattery: Boolean,
    val battery: BatteryViewModel,
    val configManager: ConfigManaging,
    val currentDevice: Device,
    val network: Networking,
    private val bannerAlertManager: BannerAlertManaging
) : ViewModel() {
    var solarStrings: List<StringPower> = listOf()
    var inverterTemperatures: InverterTemperatures? = null
    var solar: Double = 0.0
    var home: Double = 0.0
    var grid: Double = 0.0
    var ct2: Double = 0.0
    val deviceState = MutableStateFlow<DeviceState>(DeviceState.Unknown)
    val homeTotal = MutableStateFlow<Double?>(null)
    val gridImportTotal = MutableStateFlow<Double?>(null)
    val gridExportTotal = MutableStateFlow<Double?>(null)
    val earnings = MutableStateFlow<EarningsViewModel?>(null)
    val todaysGeneration = MutableStateFlow<GenerationViewModel?>(null)
    val faults = MutableStateFlow<List<String>>(listOf())
    val displayStrings = MutableStateFlow<List<StringPower>>(listOf())

    init {
        try {
            loadDeviceStatus()
            loadTotals()
        } catch (ex: Exception) {
            bannerAlertManager.showToast("Failed: ${ex.message}")
        }

        viewModelScope.launch {
            combine(configManager.themeStream, currentValuesStream) { theme, currentValues ->
                Pair(theme, currentValues)
            }.collect { (theme, currentValues) ->
                solar = currentValues.solarPower
                home = currentValues.homeConsumption
                grid = currentValues.grid
                ct2 = currentValues.ct2
                solarStrings = currentValues.solarStringsPower
                solar = currentValues.solarPower
                inverterTemperatures = currentValues.temperatures

                displayStrings.value = listOf()

                if (theme.ct2DisplayMode == CT2DisplayMode.AsPowerString) {
                    displayStrings.value = displayStrings.value.plus(StringPower("CT2", ct2))
                }

                if (theme.powerFlowStrings.enabled) {
                    displayStrings.value = displayStrings.value.plus(solarStrings)
                }
            }
        }
    }

    private suspend fun loadGeneration(): GenerationViewModel? {
        if (!shouldLoadGeneration()) {
            return null
        }

        return try {
            GenerationViewModel(
                loadHistoryData(currentDevice),
                includeCT2 = configManager.shouldCombineCT2WithPVPower,
                invertCT2 = configManager.shouldInvertCT2
            )
        } catch (_: FoxServerError) {
            null
        }
    }

    private fun shouldLoadGeneration(): Boolean {
        return configManager.totalYieldModel == TotalYieldModel.EnergyStats ||
                configManager.powerFlowStrings.enabled ||
                configManager.ct2DisplayMode == CT2DisplayMode.AsPowerString
    }

    private suspend fun loadHistoryData(device: Device): OpenHistoryResponse {
        val start = QueryDate().toUtcMillis()
        return network.fetchHistory(
            deviceSN = device.deviceSN,
            variables = listOf("meterPower2", "pv1Power", "pv2Power", "pv3Power", "pv4Power", "pv5Power", "pv6Power"),
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
            } catch (ex: Exception) {
                bannerAlertManager.showToast("Failed: ${ex.message}")
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
                    val generation = loadGeneration()
                    val totals = TotalsViewModel(loadReportData(currentDevice), generation)
                    earnings.value = EarningsViewModel(EnergyStatsFinancialModel(totals, configManager))
                    homeTotal.value = totals.loads
                    gridImportTotal.value = totals.grid
                    gridExportTotal.value = totals.feedIn
                    generation?.updatePvTotal(totals.solar)

                    todaysGeneration.value = generation
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
