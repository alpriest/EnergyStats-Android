package com.alpriest.energystats.ui.flow.home

import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.models.UnitData
import java.lang.Double.max
import java.lang.Double.min
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Locale

class GenerationViewModel(private val response: OpenHistoryResponse, private val includeCT2: Boolean, private val invertCT2: Boolean) {
    fun solarToday(): Double {
        val pvPowerVariables = response.datas.filter { it.variable == "pvPower" }
            .flatMap { it.data.toList() }
            .map { it.copy(value = max(0.0, it.value)) }
        val ct2Variables: List<UnitData>

        if (includeCT2) {
            ct2Variables = response.datas.filter { it.variable == "meterPower2" }
                .flatMap { it.data.toList() }
                .map {
                    if (invertCT2) {
                        it.copy(value = min(0.0, it.value))
                    } else {
                        it.copy(value = max(0.0, it.value))
                    }
                }
        } else {
            ct2Variables = listOf()
        }

        val filteredVariables = pvPowerVariables + ct2Variables

        val timeDifferenceInSeconds: Double = if (filteredVariables.size > 1) {
            val dateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
            val firstTime = dateFormat.parse(filteredVariables[0].time).toInstant().atZone(ZoneId.systemDefault()).toEpochSecond()
            val secondTime = dateFormat.parse(filteredVariables[1].time).toInstant().atZone(ZoneId.systemDefault()).toEpochSecond()

            (secondTime - firstTime).toDouble()
        } else {
            5.0 * 60.0
        }

        val totalSum = filteredVariables.sumOf { it.value }

        return totalSum * (timeDifferenceInSeconds / 3600.0)
    }
}