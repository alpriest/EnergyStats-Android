package com.alpriest.energystats.ui.summary

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.models.ReportVariable
import com.alpriest.energystats.models.SolcastForecastResponse
import com.alpriest.energystats.models.toHalfHourOfDay
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.paramsgraph.DateTimeFloatEntry
import com.alpriest.energystats.ui.settings.solcast.SolarForecasting
import com.alpriest.energystats.ui.theme.AppTheme
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlin.time.Duration.Companion.hours

data class SolarForecastViewData(
    val error: String?,
    val today: ChartEntryModelProducer,
    val todayTotal: Double,
    val tomorrow: ChartEntryModelProducer,
    val tomorrowTotal: Double,
    val name: String?,
    val resourceId: String
)

class SolarForecastViewModelFactory(
    private val solarForecastProvider: SolarForecasting,
    private val themeStream: MutableStateFlow<AppTheme>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SolarForecastViewModel(solarForecastProvider, themeStream) as T
    }
}

class SolarForecastViewModel(
    private val solarForecastProvider: SolarForecasting,
    private val themeStream: MutableStateFlow<AppTheme>
) : ViewModel() {
    val hasSitesStream = MutableStateFlow<Boolean>(false)
    val dataStream = MutableStateFlow<List<SolarForecastViewData>>(listOf())

    suspend fun load() {
        val settings = themeStream.value.solcastSettings

        if (settings.sites.isEmpty() || settings.apiKey == null) {
            return
        }

        dataStream.value = settings.sites.map {
            val forecasts = solarForecastProvider.fetchForecast(it, settings.apiKey).forecasts
            val today = getToday()
            val tomorrow = getTomorrow()

            val todayData = forecasts.filter { response ->
                isSameDay(response.periodEnd, today)
            }

            val tomorrowData = forecasts.filter { response ->
                isSameDay(response.periodEnd, tomorrow)
            }

            SolarForecastViewData(
                error = null,
                today = ChartEntryModelProducer(todayData.map { response ->
                    DateFloatEntry(
                        date = response.periodEnd,
                        x = response.periodEnd.toHalfHourOfDay().toFloat(),
                        y = response.pvEstimate.toFloat(),
                    )
                }),
                todayTotal = total(todayData),
                tomorrow = ChartEntryModelProducer(tomorrowData.map { response ->
                    DateFloatEntry(
                        date = response.periodEnd,
                        x = response.periodEnd.toHalfHourOfDay().toFloat(),
                        y = response.pvEstimate.toFloat(),
                    )
                }),
                tomorrowTotal = total(tomorrowData),
                name = it.name,
                resourceId = it.resourceId
            )
        }
    }

    fun total(forecasts: List<SolcastForecastResponse>): Double {
        return forecasts.fold(0.0) { total, forecast ->
            val periodHours = convertPeriodToHours(period = forecast.period)
            total + (forecast.pvEstimate * periodHours)
        }
    }

    private fun convertPeriodToHours(period: String): Double {
        // Regular expression to extract the numeric value from the period string (assuming format "PT30M")
        val regex = """(\d+)""".toRegex()
        val matchResult = regex.find(period)

        return matchResult?.let {
            val periodMinutes = it.groupValues[1].toDoubleOrNull()
            periodMinutes?.div(60.0) ?: 0.0  // Convert minutes to hours, defaulting to 0.0 if null
        } ?: 0.0
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

class DateFloatEntry(
    val date: Date,
    override val x: Float,
    override val y: Float
) : ChartEntry {
    override fun withY(y: Float): ChartEntry = DateFloatEntry(
        date = date,
        x = x,
        y = y
    )
}
