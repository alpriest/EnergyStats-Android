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
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        val sorted: List<ParsedRawResponse> = raw.map { rawResponse ->
            ParsedRawResponse(
                variable = rawResponse.variable,
                data = rawResponse.data.mapNotNull { rawData ->
                    val formatted = formatter.parse(rawData.time) ?: return@mapNotNull null

                    ParsedRawData(
                        time = formatted,
                        value = rawData.value
                    )
                }.sortedBy { it.time }
            )
        }
        solar = java.lang.Double.max(
            0.0,
            sorted.currentValue(RawVariable.LoadsPower) + sorted.currentValue(RawVariable.BatChargePower) + sorted.currentValue(RawVariable.FeedInPower) - sorted.currentValue(
                RawVariable.GridConsumptionPower
            ) - sorted.currentValue(
                RawVariable.BatDischargePower
            )
        )
        grid = sorted.currentValue(RawVariable.FeedInPower) - sorted.currentValue(RawVariable.GridConsumptionPower)
        home = sorted.currentValue(RawVariable.GridConsumptionPower) + sorted.currentValue(RawVariable.GenerationPower)
    }

    val batteryViewModel: BatteryPowerViewModel =
        BatteryPowerViewModel(configManager, batteryStateOfCharge, battery)
}

private fun List<ParsedRawResponse>.currentValue(forKey: RawVariable): Double {
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

private data class ParsedRawResponse(
    val variable: String,
    val data: List<ParsedRawData>
)

private data class ParsedRawData(
    val time: Date,
    val value: Double
)