package com.alpriest.energystats.ui.flow.home

import androidx.lifecycle.ViewModel
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.battery.BatteryPowerViewModel

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
    val home: Double = raw.currentValue(RawVariable.GridConsumptionPower) + raw.currentValue(RawVariable.GenerationPower)
    val grid: Double = raw.currentValue(RawVariable.FeedInPower) - raw.currentValue(RawVariable.GridConsumptionPower)
    val batteryViewModel: BatteryPowerViewModel =
        BatteryPowerViewModel(configManager, batteryStateOfCharge, battery, batteryTemperature)
}

private fun List<RawResponse>.currentValue(forKey: RawVariable): Double {
    var result: Double

    val item = firstOrNull { it.variable == forKey.networkTitle() }
    if (item != null) {
        item.let {
            result = it.data
                .lastOrNull()?.value ?: 0.0
        }
    } else {
        result = 0.0
    }

    return result
}
