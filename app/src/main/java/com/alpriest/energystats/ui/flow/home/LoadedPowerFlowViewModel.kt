package com.alpriest.energystats.ui.flow.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.QueryDate
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.EarningsViewModel
import com.alpriest.energystats.ui.flow.EnergyStatsFinancialModel
import com.alpriest.energystats.ui.flow.StringPower
import com.alpriest.energystats.ui.flow.TotalsViewModel
import com.alpriest.energystats.ui.flow.battery.BatteryPowerViewModel
import com.alpriest.energystats.ui.statsgraph.ReportType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

const val dateFormat = "yyyy-MM-dd HH:mm:ss"

data class InverterTemperatures(
    val ambient: Double,
    val inverter: Double
)

class LoadedPowerFlowViewModel(
    val solar: Double,
    val solarStrings: List<StringPower>,
    val home: Double,
    val grid: Double,
    val todaysGeneration: GenerationViewModel,
    val inverterTemperatures: InverterTemperatures?,
    val hasBattery: Boolean,
    val battery: BatteryViewModel,
    val configManager: ConfigManaging,
    val ct2: Double,
    val faults: List<String>,
    val currentDevice: Device,
    val network: Networking
) : ViewModel() {
    val deviceState = MutableStateFlow<DeviceState>(DeviceState.Unknown)
    val homeTotal = MutableStateFlow<Double?>(null)
    val gridImportTotal = MutableStateFlow<Double?>(null)
    val gridExportTotal = MutableStateFlow<Double?>(null)
    val earnings = MutableStateFlow<EarningsViewModel?>(null)

    init {
        loadDeviceStatus()
        loadTotals()
    }

    private fun loadDeviceStatus() {
        viewModelScope.launch {
            deviceState.value = loadDeviceStatus(currentDevice)
        }
    }

    private suspend fun loadDeviceStatus(currentDevice: Device): DeviceState {
        val device = network.fetchDevice(currentDevice.deviceSN)
        return DeviceState.fromInt(device.status)
    }

    private fun loadTotals() {
        if (configManager.showHomeTotal || configManager.showGridTotals || configManager.showFinancialSummary) {
            viewModelScope.launch {
                val totals = TotalsViewModel(loadReportData(currentDevice))
                earnings.value = EarningsViewModel(EnergyStatsFinancialModel(totals, configManager))
                homeTotal.value = totals.loads
                gridImportTotal.value = totals.grid
                gridExportTotal.value = totals.feedIn
            }
        }
    }

    private suspend fun loadReportData(curentDevice: Device): List<OpenReportResponse> {
        var reportVariables = listOf(ReportVariable.Loads, ReportVariable.FeedIn, ReportVariable.GridConsumption)
        if (curentDevice.hasBattery) {
            reportVariables = reportVariables.plus(listOf(ReportVariable.ChargeEnergyToTal, ReportVariable.DischargeEnergyToTal))
        }

        return network.fetchReport(
            curentDevice.deviceSN,
            reportVariables,
            QueryDate(),
            ReportType.month
        )
    }

    val batteryViewModel: BatteryPowerViewModel? = if (hasBattery)
        BatteryPowerViewModel(configManager, battery.chargeLevel, battery.chargePower, battery.temperature, battery.residual)
    else
        null
}
