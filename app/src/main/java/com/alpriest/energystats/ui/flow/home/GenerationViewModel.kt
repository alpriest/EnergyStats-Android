package com.alpriest.energystats.ui.flow.home

import com.alpriest.energystats.models.OpenHistoryResponse
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Locale
import kotlin.math.abs

class GenerationViewModel(pvTotal: Double, response: OpenHistoryResponse, includeCT2: Boolean, invertCT2: Boolean) {
    val solarToday: Double

    init {
        if (includeCT2) {
            val dateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
            val timeZone = ZoneId.systemDefault()
            val ct2Total = response.datas.filter { it.variable == "meterPower2" }
                .flatMap { it.data.toList() }
                .mapNotNull {
                    if (invertCT2) {
                        if (it.value < 0) {
                            it.copy(value = abs(it.value))
                        } else {
                            null
                        }
                    } else {
                        if (it.value > 0) {
                            it.copy(value = abs(it.value))
                        } else {
                            null
                        }
                    }
                }
                .zipWithNext()
                .mapNotNull { (a, b) ->
                    val timeA = dateFormat.parse(a.time)?.toInstant()?.atZone(timeZone)?.toEpochSecond()
                    val timeB = dateFormat.parse(b.time)?.toInstant()?.atZone(timeZone)?.toEpochSecond()

                    if (timeA != null && timeB != null) {
                        val dt = timeB - timeA
                        val averageValue = (a.value + b.value) / 2  // Trapezoidal rule; optional
                        averageValue * dt / 3600.0  // Convert seconds to hours
                    } else {
                        null
                    }
                }
                .sum()

            solarToday = pvTotal + ct2Total
        } else {
            solarToday = pvTotal
        }
    }
}