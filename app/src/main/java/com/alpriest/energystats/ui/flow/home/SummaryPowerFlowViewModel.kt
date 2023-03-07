package com.alpriest.energystats.ui.flow.home

import androidx.lifecycle.ViewModel
import com.alpriest.energystats.models.RawData
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.battery.BatteryPowerViewModel
import java.text.SimpleDateFormat
import java.util.*

const val dateFormat = "yyyy-MM-dd HH:mm:ss"

class SummaryPowerFlowViewModel(
    val configManager: ConfigManaging,
    val battery: Double,
    val batteryStateOfCharge: Double,
    val hasBattery: Boolean,
    val raw: List<RawResponse>,
    val batteryTemperature: Double
) : ViewModel() {
    val solar: Double = java.lang.Double.max(
        0.0,
        raw.currentValue(RawVariable.LoadsPower) + raw.currentValue(RawVariable.BatChargePower) + raw.currentValue(RawVariable.FeedInPower) - raw.currentValue(
            RawVariable.GridConsumptionPower
        ) - raw.currentValue(
            RawVariable.BatDischargePower
        )
    )
    val home: Double = raw.currentValue(RawVariable.GridConsumptionPower) + raw.currentValue(RawVariable.GenerationPower) - raw.currentValue(RawVariable.FeedInPower)
    val grid: Double = raw.currentValue(RawVariable.FeedInPower) - raw.currentValue(RawVariable.GridConsumptionPower)
    val batteryViewModel: BatteryPowerViewModel = BatteryPowerViewModel(configManager, batteryStateOfCharge, battery, batteryTemperature)
    val latestUpdate = raw.currentData(RawVariable.GridConsumptionPower)?.time?.let { SimpleDateFormat(dateFormat, Locale.getDefault()).parse(it) } ?: Date()
}

private fun List<RawResponse>.currentValue(forKey: RawVariable): Double {
    val item = currentData(forKey)
    return item?.value ?: 0.0
}

private fun List<RawResponse>.currentData(forKey: RawVariable): RawData? {
    return firstOrNull { it.variable == forKey.networkTitle() }?.data?.lastOrNull()
}
