package com.alpriest.energystats.models

import com.google.gson.annotations.SerializedName
import java.util.Calendar
import java.util.Date

data class SolcastForecastResponseList(
    val forecasts: List<SolcastForecastResponse>
)

data class SolcastForecastResponse(
    @SerializedName("pv_estimate")
    val pvEstimate: Double,
    @SerializedName("pv_estimate10")
    val pvEstimate10: Double,
    @SerializedName("pv_estimate90")
    val pvEstimate90: Double,
    @SerializedName("period_end")
    val periodEnd: Date,
    val period: String
)

fun Date.toHalfHourOfDay(): Int {
    val calendar = Calendar.getInstance().apply { time = this@toHalfHourOfDay }
    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val minutes = calendar.get(Calendar.MINUTE)

    return hours * 2 + if (minutes >= 30) 1 else 0
}