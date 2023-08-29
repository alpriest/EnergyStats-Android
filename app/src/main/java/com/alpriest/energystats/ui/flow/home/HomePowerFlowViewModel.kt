package com.alpriest.energystats.ui.flow.home

import androidx.lifecycle.ViewModel
import com.alpriest.energystats.models.BatteryViewModel
import com.alpriest.energystats.models.RawData
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.models.ReportData
import com.alpriest.energystats.models.ReportResponse
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.battery.BatteryPowerViewModel
import java.util.Calendar

const val dateFormat = "yyyy-MM-dd HH:mm:ss"

data class InverterTemperatures(
    val ambient: Double,
    val inverter: Double
)

data class InverterViewModel(
    val temperatures: InverterTemperatures,
    val name: String
)

class HomePowerFlowViewModel(
    val solar: Double,
    val home: Double,
    val grid: Double,
    val todaysGeneration: Double,
    val earnings: String,
    val inverterViewModel: InverterViewModel?,
    val hasBattery: Boolean,
    val report: List<ReportResponse>,
    val battery: BatteryViewModel,
    val configManager: ConfigManaging
) : ViewModel() {
    val homeTotal: Double = report.todayValue(forKey = "loads")
    val batteryViewModel: BatteryPowerViewModel? = if (hasBattery)
        BatteryPowerViewModel(configManager, battery.chargeLevel, battery.chargePower, battery.temperature, battery.residual)
    else
        null
}

private fun List<ReportResponse>.todayValue(forKey: String): Double {
    val item = currentData(forKey)
    return item?.value ?: 0.0
}

private fun List<ReportResponse>.currentData(forKey: String): ReportData? {
    val todaysDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    return firstOrNull { it.variable.lowercase() == forKey.lowercase() }?.data?.firstOrNull { it.index == todaysDate }
}
