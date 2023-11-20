package com.alpriest.energystats.ui.summary

import com.google.gson.annotations.SerializedName
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.ZoneId
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
        val today = getToday()
        val tomorrow = getTomorrow()

        this.today.value = forecasts.filter {
            isSameDay(it.periodEnd, today)
        }

        this.tomorrow.value = forecasts.filter {
            isSameDay(it.periodEnd, tomorrow)
        }
    }

    fun isSameDay(date1: Date, date2: Date): Boolean {
        val localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

        return localDate1 == localDate2
    }

    private fun getTomorrow(): Date {
        val date = LocalDate.now().plusDays(1)
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    private fun getToday(): Date {
        val date = LocalDate.now()
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }
}

