package com.alpriest.energystats.ui.flow.home

import androidx.lifecycle.ViewModel
import com.alpriest.energystats.models.RawData
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.battery.BatteryPowerViewModel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

const val dateFormat = "yyyy-MM-dd HH:mm:ss"

class SummaryPowerFlowViewModel(
    val configManager: ConfigManaging,
    val batteryChargePower: Double,
    val batteryStateOfCharge: Double,
    val raw: List<RawResponse>,
    val batteryTemperature: Double,
    val todaysGeneration: Double,
    val batteryResidual: Int,
    val hasBattery: Boolean,
    val earnings: String
) : ViewModel() {
    val solar: Double = java.lang.Double.max(
        0.0,
        raw.currentValue("loadsPower") + raw.currentValue("batChargePower") + raw.currentValue("feedInPower") - raw.currentValue(
            "gridConsumptionPower"
        ) - raw.currentValue(
            "batDischargePower"
        )
    )
    val home: Double = raw.currentValue("gridConsumptionPower") + raw.currentValue("generationPower") - raw.currentValue("feedInPower")
    val grid: Double = raw.currentValue("feedInPower") - raw.currentValue("gridConsumptionPower")
    val batteryViewModel: BatteryPowerViewModel? = if (hasBattery)
        BatteryPowerViewModel(configManager, batteryStateOfCharge, batteryChargePower, batteryTemperature, batteryResidual, configManager.minSOC.value ?: 0.0)
    else
        null
    val latestUpdate = raw.currentData("gridConsumptionPower")?.time?.let {
        SimpleDateFormat(dateFormat, Locale.getDefault()).parse(it)?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
    } ?: LocalDateTime.now()
}

private fun List<RawResponse>.currentValue(forKey: String): Double {
    val item = currentData(forKey)
    return item?.value ?: 0.0
}

private fun List<RawResponse>.currentData(forKey: String): RawData? {
    return firstOrNull { it.variable.lowercase() == forKey.lowercase() }?.data?.lastOrNull()
}
