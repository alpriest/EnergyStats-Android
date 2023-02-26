package com.alpriest.energystats.models

import java.lang.Double.max
import java.text.SimpleDateFormat
import java.util.*

class HistoricalViewModel(raw: Array<RawResponse>) {
    var currentGridExport: Double
    val currentHomeConsumption: Double
    val currentSolarPower: Double

    init {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
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
        currentSolarPower = max(
            0.0,
            sorted.currentValue(RawVariable.LoadsPower) + sorted.currentValue(RawVariable.BatChargePower) + sorted.currentValue(RawVariable.FeedInPower) - sorted.currentValue(
                RawVariable.GridConsumptionPower
            ) - sorted.currentValue(
                RawVariable.BatDischargePower
            )
        )
        currentGridExport = sorted.currentValue(RawVariable.FeedInPower) - sorted.currentValue(RawVariable.GridConsumptionPower)
        currentHomeConsumption = sorted.currentValue(RawVariable.GridConsumptionPower) + sorted.currentValue(RawVariable.GenerationPower)
    }
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

