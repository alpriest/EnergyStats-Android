package com.alpriest.energystats.ui.flow.home

import com.alpriest.energystats.models.OpenHistoryResponse
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Locale
import kotlin.math.abs

class GenerationViewModel(private val pvTotal: Double, private val response: OpenHistoryResponse, private val includeCT2: Boolean, private val invertCT2: Boolean) {
    fun solarToday(): Double {
        val ct2Total: Double

        if (includeCT2) {
            val ct2Variables = response.datas.filter { it.variable == "meterPower2" }
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

            val timeDifferenceInSeconds: Double = if (ct2Variables.size > 1) {
                val dateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
                val firstTime = dateFormat.parse(ct2Variables[0].time).toInstant().atZone(ZoneId.systemDefault()).toEpochSecond()
                val secondTime = dateFormat.parse(ct2Variables[1].time).toInstant().atZone(ZoneId.systemDefault()).toEpochSecond()

                (secondTime - firstTime).toDouble()
            } else {
                5.0 * 60.0
            }

            ct2Total = ct2Variables.sumOf { it.value } * (timeDifferenceInSeconds / 3600.0)
        } else {
            ct2Total = 0.0
        }

        return pvTotal + ct2Total
    }
}