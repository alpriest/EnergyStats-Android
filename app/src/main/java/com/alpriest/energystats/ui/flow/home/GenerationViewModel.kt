package com.alpriest.energystats.ui.flow.home

import com.alpriest.energystats.models.OpenHistoryResponse
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Locale

class GenerationViewModel(private val response: OpenHistoryResponse) {
    fun todayGeneration(): Double {
        val filteredVariables = response.datas.filter { it.variable == "pvPower" }.flatMap { it.data.toList() }

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