package com.alpriest.energystats.models

import java.lang.Double.max

class HistoricalViewModel(raw: Array<RawResponse>) {
    var currentGridExport: Double
    val currentHomeConsumption: Double
    val currentSolarPower: Double

    init {
        currentSolarPower = max(
            0.0,
            raw.currentValue(RawVariable.LoadsPower) + raw.currentValue(RawVariable.BatChargePower) + raw.currentValue(RawVariable.FeedinPower) - raw.currentValue(RawVariable.GridConsumptionPower) - raw.currentValue(
                RawVariable.BatDischargePower
            )
        )
        currentGridExport = raw.currentValue(RawVariable.FeedinPower) - raw.currentValue(RawVariable.GridConsumptionPower)
        currentHomeConsumption = raw.currentValue(RawVariable.GridConsumptionPower) + raw.currentValue(RawVariable.GenerationPower)
    }
}

fun Array<RawResponse>.currentValue(forKey: RawVariable): Double {
    var result: Double

    val item = firstOrNull { it.variable == forKey.networkTitle() }
    if (item != null) {
        item.let { result = it.data.lastOrNull()?.value ?: 0.0 }
    } else {
        result = 0.0
    }

    return result
}