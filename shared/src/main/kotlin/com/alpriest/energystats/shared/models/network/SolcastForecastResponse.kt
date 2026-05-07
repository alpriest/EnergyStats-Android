package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Calendar
import java.util.Date
import kotlin.time.Instant

@Serializable
data class SolcastForecastResponseList(
    val forecasts: List<SolcastForecastResponse>
)

@Serializable
data class SolcastForecastResponse(
    @SerialName("pv_estimate")
    val pvEstimate: Double,
    @SerialName("pv_estimate10")
    val pvEstimate10: Double,
    @SerialName("pv_estimate90")
    val pvEstimate90: Double,
    @SerialName("period_end")
    val periodEnd: Instant,
    val period: String
)

fun Date.toHalfHourOfDay(): Int {
    val calendar = Calendar.getInstance().apply { time = this@toHalfHourOfDay }
    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val minutes = calendar.get(Calendar.MINUTE)

    return hours * 2 + if (minutes >= 30) 1 else 0
}
