package com.alpriest.energystats.ui.flow.home

import androidx.lifecycle.ViewModel
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
    raw: List<RawResponse>
) : ViewModel() {
    val solar: Double
    val home: Double
    val grid: Double

    init {
        solar = java.lang.Double.max(
            0.0,
            raw.currentValue(RawVariable.LoadsPower) + raw.currentValue(RawVariable.BatChargePower) + raw.currentValue(RawVariable.FeedInPower) - raw.currentValue(
                RawVariable.GridConsumptionPower
            ) - raw.currentValue(
                RawVariable.BatDischargePower
            )
        )
        grid = raw.currentValue(RawVariable.FeedInPower) - raw.currentValue(RawVariable.GridConsumptionPower)
        home = raw.currentValue(RawVariable.GridConsumptionPower) + raw.currentValue(RawVariable.GenerationPower)
    }

    val batteryViewModel: BatteryPowerViewModel =
        BatteryPowerViewModel(configManager, batteryStateOfCharge, battery)
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
