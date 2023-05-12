package com.alpriest.energystats.ui.flow.home

import androidx.lifecycle.ViewModel
import com.alpriest.energystats.models.RawData
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.battery.BatteryPowerViewModel
import java.text.SimpleDateFormat
import java.util.*

const val dateFormat = "yyyy-MM-dd HH:mm:ss"

class SummaryPowerFlowViewModel(
    val configManager: ConfigManaging,
    val battery: Double,
    val batteryStateOfCharge: Double,
    val raw: List<RawResponse>,
    val batteryTemperature: Double,
    val todaysGeneration: Double
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
    val batteryViewModel: BatteryPowerViewModel = BatteryPowerViewModel(configManager, batteryStateOfCharge, battery, batteryTemperature)
    val latestUpdate = raw.currentData("gridConsumptionPower")?.time?.let { SimpleDateFormat(dateFormat, Locale.getDefault()).parse(it) } ?: Date()
}

private fun List<RawResponse>.currentValue(forKey: String): Double {
    val item = currentData(forKey)
    return item?.value ?: 0.0
}

private fun List<RawResponse>.currentData(forKey: String): RawData? {
    return firstOrNull { it.variable.lowercase() == forKey.lowercase() }?.data?.lastOrNull()
}
