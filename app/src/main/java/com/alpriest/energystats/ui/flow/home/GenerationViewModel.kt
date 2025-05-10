package com.alpriest.energystats.ui.flow.home

import com.alpriest.energystats.models.OpenHistoryResponse
import com.alpriest.energystats.ui.flow.StringType
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Locale
import kotlin.math.abs

class GenerationViewModel(private val pvTotal: Double, response: OpenHistoryResponse, private val includeCT2: Boolean, private val invertCT2: Boolean) {
    private val pv1Total: Double = response.trapezoidalAverage("pv1Power")
    private val pv2Total: Double = response.trapezoidalAverage("pv2Power")
    private val pv3Total: Double = response.trapezoidalAverage("pv3Power")
    private val pv4Total: Double = response.trapezoidalAverage("pv4Power")
    private val pv5Total: Double = response.trapezoidalAverage("pv5Power")
    private val pv6Total: Double = response.trapezoidalAverage("pv6Power")
    private val ct2Total: Double

    init {
        val dateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
        val timeZone = ZoneId.systemDefault()
        ct2Total = response.datas.filter { it.variable == "meterPower2" }
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
    }

    val todayGeneration: Double
        get() = pvTotal + (if (includeCT2) ct2Total else 0.0)

    fun estimatedTotal(string: StringType): Double {
        return when (string) {
            StringType.PV1 -> pv1Total
            StringType.PV2 -> pv2Total
            StringType.PV3 -> pv3Total
            StringType.PV4 -> pv4Total
            StringType.PV5 -> pv5Total
            StringType.PV6 -> pv6Total
            StringType.CT2 -> ct2Total
        }
    }
}

private fun OpenHistoryResponse.trapezoidalAverage(key: String): Double {
    val dateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
    val timeZone = ZoneId.systemDefault()
    return datas
        .filter { it.variable == key }
        .flatMap { it.data.toList() }
        .sortedBy { it.time }
        .zipWithNext()
        .mapNotNull { (a, b) ->
            val timeA = dateFormat.parse(a.time)?.toInstant()?.atZone(timeZone)?.toEpochSecond()
            val timeB = dateFormat.parse(b.time)?.toInstant()?.atZone(timeZone)?.toEpochSecond()
            if (timeA != null && timeB != null) {
                val dt = timeB - timeA
                val averageValue = (a.value + b.value) / 2.0
                averageValue * dt / 3600.0
            } else {
                null
            }
        }
        .sum()
}