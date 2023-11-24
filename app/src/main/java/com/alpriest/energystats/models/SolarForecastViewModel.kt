package com.alpriest.energystats.models

import com.google.gson.annotations.SerializedName
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