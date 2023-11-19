package com.alpriest.energystats.ui.summary

import com.google.gson.annotations.SerializedName
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.MutableStateFlow
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
    val periodEnd: Date
)

interface SolarForecasting {
    suspend fun fetchForecast(): SolcastForecastResponseList
}

class SolarForecastViewModel(private val solarForecastProvider: SolarForecasting) {
    val producer: ChartEntryModelProducer = ChartEntryModelProducer()
    val today = MutableStateFlow<List<SolcastForecastResponse>>(listOf())
    val tomorrow = MutableStateFlow<List<SolcastForecastResponse>>(listOf())

    suspend fun load() {
        val forecasts = solarForecastProvider.fetchForecast().forecasts
    }
}