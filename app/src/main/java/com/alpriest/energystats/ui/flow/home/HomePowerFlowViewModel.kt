package com.alpriest.energystats.ui.flow.home

import androidx.lifecycle.ViewModel
import com.alpriest.energystats.models.RawData
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.models.ReportData
import com.alpriest.energystats.models.ReportResponse
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.battery.BatteryPowerViewModel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale

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
    val configManager: ConfigManaging,
    val batteryChargePower: Double,
    val batteryStateOfCharge: Double,
    val raw: List<RawResponse>,
    val batteryTemperature: Double,
    val todaysGeneration: Double,
    val batteryResidual: Int,
    val hasBattery: Boolean,
    val earnings: String,
    val report: List<ReportResponse>,
    val solar: Double,
    val home: Double
) : ViewModel() {
    val homeTotal: Double = report.todayValue(forKey = "loads")
    val grid: Double = raw.currentValue("feedInPower") - raw.currentValue("gridConsumptionPower")
    val batteryViewModel: BatteryPowerViewModel? = if (hasBattery)
        BatteryPowerViewModel(configManager, batteryStateOfCharge, batteryChargePower, batteryTemperature, batteryResidual, configManager.minSOC.value ?: 0.0)
    else
        null
    val latestUpdate: LocalDateTime = raw.currentData("gridConsumptionPower")?.time?.let {
        SimpleDateFormat(dateFormat, Locale.getDefault()).parse(it)?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
    } ?: LocalDateTime.now()
    val inverterViewModel = makeInverterViewModel()

    private fun makeInverterViewModel(): InverterViewModel? {
        return if (raw.find { it.variable == "ambientTemperation" } != null &&
            raw.find { it.variable == "invTemperation"} != null) {
            val temperatures = InverterTemperatures(raw.currentValue("ambientTemperation"), raw.currentValue("invTemperation"))
            InverterViewModel(temperatures, name = configManager.currentDevice.value?.deviceType ?: "")
        } else {
            null
        }
    }
}

private fun List<ReportResponse>.todayValue(forKey: String): Double {
    val item = currentData(forKey)
    return item?.value ?: 0.0
}

private fun List<ReportResponse>.currentData(forKey: String): ReportData? {
    val todaysDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    return firstOrNull { it.variable.lowercase() == forKey.lowercase() }?.data?.firstOrNull { it.index == todaysDate }
}

private fun List<RawResponse>.currentValue(forKey: String): Double {
    val item = currentData(forKey)
    return item?.value ?: 0.0
}

private fun List<RawResponse>.currentData(forKey: String): RawData? {
    return firstOrNull { it.variable.lowercase() == forKey.lowercase() }?.data?.lastOrNull()
}
