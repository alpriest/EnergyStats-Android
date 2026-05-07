package com.alpriest.energystats.ui.summary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.alpriest.energystats.R
import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.helpers.asPercent
import com.alpriest.energystats.shared.helpers.fullDateTime
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.shared.models.QueryDate
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.shared.models.network.ReportType
import com.alpriest.energystats.shared.models.network.SolcastFailure
import com.alpriest.energystats.shared.models.network.SolcastForecastResponse
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.time.Instant
import kotlin.time.toJavaInstant

enum class SolarForecastPeriod {
    LastWeek,
    Yesterday
}

data class SolarForecastTotalData(
    val total: Double,
    val percentageTimePeriodsAvailable: Double
)

data class PercentageSolarForecastAchievedData(
    val totalSolarForecast: Double,
    val totalSolarAchieved: Double,
    val percentageSolarForecastAchieved: Double,
    val description: String,
    val forecastCompleteness: Double
)

data class SolarForecastViewData(
    val error: String?,
    val today: List<List<DateFloatEntry>>,
    val todayTotal: Double,
    val tomorrow: List<List<DateFloatEntry>>,
    val tomorrowTotal: Double,
    val name: String?,
    val resourceId: String
)

class SolarForecastViewModelFactory(
    private val solarForecastProvider: () -> SolcastCaching,
    private val appSettingsStream: StateFlow<AppSettings>,
    private val configManager: ConfigManaging,
    private val networking: Networking,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SolarForecastViewModel(solarForecastProvider, appSettingsStream, configManager, networking, application) as T
    }
}

class SolarForecastViewModel(
    private val solarForecastProvider: () -> SolcastCaching,
    private val appSettingsStream: StateFlow<AppSettings>,
    private val configManager: ConfigManaging,
    private val networking: Networking,
    application: Application
) : AndroidViewModel(application) {
    val dataStream = MutableStateFlow<List<SolarForecastViewData>>(listOf())
    var loadStateStream = MutableStateFlow<LoadState>(LoadState.Inactive)
    var tooManyRequestsStream = MutableStateFlow(false)
    var canRefreshStream = MutableStateFlow(true)
    var lastFetchedStream = MutableStateFlow<String?>(null)
    val solarForecastAchievedDataStream = MutableStateFlow<PercentageSolarForecastAchievedData?>(null)
    val periodStream = MutableStateFlow(SolarForecastPeriod.LastWeek)

    fun load(ignoreCache: Boolean = false) {
        viewModelScope.launch {
            updateCanRefresh()
            if (loadStateStream.value != LoadState.Inactive) {
                return@launch
            }
            val settings = appSettingsStream.value.solcastSettings
            if (settings.sites.isEmpty()) {
                return@launch
            }
            val apiKey = settings.apiKey ?: return@launch

            loadStateStream.value = LoadState.Active.Loading

            try {
                val allForecasts = mutableListOf<SolcastForecastResponse>()
                dataStream.value = settings.sites.mapNotNull { site ->
                    val forecast = solarForecastProvider().fetchForecast(site, apiKey, ignoreCache)
                    lastFetchedStream.value = configManager.lastSolcastRefresh?.fullDateTime()

                    if (forecast.failure == null) {
                        val forecasts = forecast.forecasts
                        allForecasts.addAll(forecasts)
                        val today = LocalDate.now()
                        val tomorrow = today.plusDays(1)

                        val todayData = forecasts.filter { response ->
                            response.periodEnd.toLocalDate(ZoneId.systemDefault()) == today
                        }

                        val tomorrowData = forecasts.filter { response ->
                            response.periodEnd.toLocalDate(ZoneId.systemDefault()) == tomorrow
                        }

                        SolarForecastViewData(
                            error = null,
                            today = asGraphData(todayData),
                            todayTotal = todayData.total(),
                            tomorrow = asGraphData(tomorrowData),
                            tomorrowTotal = tomorrowData.total(),
                            name = site.name,
                            resourceId = site.resourceId
                        )
                    } else {
                        forecast.failure?.let {
                            when (it) {
                                SolcastFailure.TooManyRequests ->
                                    loadStateStream.value = LoadState.Error(null, application.getString(R.string.could_not_load_forecast_you_have_exceeded_your_free_daily_limit))

                                is SolcastFailure.Unknown ->
                                    loadStateStream.value = LoadState.Error(it.error, application.getString(R.string.unknown_error))
                            }
                        }
                        null
                    }
                }

                solarForecastAchievedDataStream.value = calculateSolarForecastAchieved(allForecasts, settings.sites.count())
                loadStateStream.value = LoadState.Inactive
            } catch (ex: Exception) {
                loadStateStream.value = LoadState.Error(ex, ex.localizedMessage ?: application.getString(R.string.unknown_error))
                solarForecastProvider().clearCache()
            }
        }
    }

    fun toggleSolarVsGenerationPeriod() {
        periodStream.value = if (periodStream.value == SolarForecastPeriod.Yesterday) SolarForecastPeriod.LastWeek else SolarForecastPeriod.Yesterday
        load()
    }

    private suspend fun calculateSolarForecastAchieved(
        forecasts: MutableList<SolcastForecastResponse>,
        siteCount: Int
    ): PercentageSolarForecastAchievedData {
        val (startDate, endDate) = dates()
        val totalSolarAchieved = calculateSolarGenerated(startDate, endDate)
        val solarForecastTotalData = calculateSolarForecastTotal(
            startDate,
            endDate,
            forecasts = forecasts,
            siteCount = siteCount
        )

        val totalSolarForecast = solarForecastTotalData.total
        val percentageSolarForecastAchieved: Double = if (totalSolarForecast > 0) (totalSolarAchieved / totalSolarForecast) else 0.0
        val coverage = solarForecastTotalData.percentageTimePeriodsAvailable

        val description = String.format(
            "%s of required Solcast data is available. %s of forecast generated.",
            coverage.asPercent(),
            percentageSolarForecastAchieved.asPercent()
        )

        return PercentageSolarForecastAchievedData(
            totalSolarForecast = totalSolarForecast,
            totalSolarAchieved = totalSolarAchieved,
            percentageSolarForecastAchieved = percentageSolarForecastAchieved,
            description = description,
            forecastCompleteness = coverage
        )
    }

    private fun calculateSolarForecastTotal(
        startDate: LocalDate,
        endDate: LocalDate,
        forecasts: MutableList<SolcastForecastResponse>,
        siteCount: Int
    ): SolarForecastTotalData {
        val startDate = startDate.atStartOfDay().toLocalDate()
        val endDate = endDate.atStartOfDay().toLocalDate()
        val zone = ZoneId.systemDefault()
        val filtered = forecasts.filter { forecast ->
            val forecastDay = forecast.periodEnd.toLocalDate(zone)
            forecastDay in startDate..endDate
        }
        val forecastsCount = filtered.count() / siteCount
        val total = filtered.total()
        val expectedPeriodCount = expectedThirtyMinutePeriodCount(startDate, endDate)
        val percentageTimePeriodsAvailable = if (expectedPeriodCount > 0) (forecastsCount.toDouble() / expectedPeriodCount.toDouble()) else 0.0

        return SolarForecastTotalData(
            total,
            percentageTimePeriodsAvailable
        )
    }

    private fun expectedThirtyMinutePeriodCount(startDate: LocalDate, endDate: LocalDate): Long {
        val days = ChronoUnit.DAYS.between(startDate, endDate) + 1
        return days * 48
    }

    private suspend fun calculateSolarGenerated(startDate: LocalDate, endDate: LocalDate): Double {
        val deviceSN = configManager.selectedDeviceSN ?: return 0.0
        val days = generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(endDate) }
            .toList()

        val monthStartDates = days
            .map { it.withDayOfMonth(1) }
            .toSet()
            .toList()
        var totalSolarAchieved = 0.0

        for (monthStartDate in monthStartDates) {
            val rawData = networking.fetchReport(
                deviceSN,
                listOf(ReportVariable.PvEnergyToTal),
                QueryDate.from(monthStartDate),
                ReportType.month
            ).getOrNull(0)

            rawData?.let {
                for (day in days.filter { it.year == monthStartDate.year && it.month == monthStartDate.month }) {
                    val dayIndex = day.dayOfMonth - 1

                    if (rawData.values.size <= dayIndex) continue

                    val value = rawData.values[dayIndex].value

                    totalSolarAchieved += value
                }
            }
        }

        return totalSolarAchieved
    }

    private fun dates(): Pair<LocalDate, LocalDate> {
        val period = periodStream.value
        val today = LocalDate.now()
        val startDate: LocalDate
        val endDate: LocalDate

        when (period) {
            SolarForecastPeriod.Yesterday -> {
                val yesterday = today.minusDays(1)
                startDate = yesterday
                endDate = yesterday
            }

            SolarForecastPeriod.LastWeek -> {
                val firstDay = today.minusDays(6)
                startDate = firstDay
                endDate = today
            }
        }

        return startDate to endDate
    }

    private fun asGraphData(data: List<SolcastForecastResponse>): List<List<DateFloatEntry>> {
        return listOf(
            data.map { response ->
                DateFloatEntry(
                    date = Date.from(response.periodEnd.toJavaInstant()),
                    x = response.periodEnd.toHalfHourOfDay(ZoneId.systemDefault()).toFloat(),
                    y = response.pvEstimate90.toFloat(),
                )
            },
            data.map { response ->
                DateFloatEntry(
                    date = Date.from(response.periodEnd.toJavaInstant()),
                    x = response.periodEnd.toHalfHourOfDay(ZoneId.systemDefault()).toFloat(),
                    y = response.pvEstimate10.toFloat(),
                )
            },
            data.map { response ->
                DateFloatEntry(
                    date = Date.from(response.periodEnd.toJavaInstant()),
                    x = response.periodEnd.toHalfHourOfDay(ZoneId.systemDefault()).toFloat(),
                    y = response.pvEstimate.toFloat(),
                )
            }
        )
    }

    private fun getTomorrow(): Date {
        val date = LocalDate.now().plusDays(1)
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    fun refetchSolcast() {
        viewModelScope.launch {
            load(ignoreCache = true)
        }
    }

    private fun updateCanRefresh() {
        val lastSolcastRefresh = configManager.lastSolcastRefresh

        if (lastSolcastRefresh == null) {
            canRefreshStream.value = false
        } else {
            val oneHourInMillis: Long = 1 * 60 * 60 * 1000
            canRefreshStream.value = System.currentTimeMillis() - lastSolcastRefresh.toInstant(ZoneOffset.UTC).toEpochMilli() > oneHourInMillis
        }
    }
}

fun getToday(): Date {
    val date = LocalDate.now()
    return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
}

fun Instant.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate {
    return toJavaInstant().atZone(zone).toLocalDate()
}

fun Instant.toHalfHourOfDay(zone: ZoneId = ZoneId.systemDefault()): Int {
    val localTime = toJavaInstant().atZone(zone).toLocalTime()
    return localTime.hour * 2 + localTime.minute / 30
}

fun isSameDay(
    instant: Instant,
    localDate: LocalDate,
    zone: ZoneId = ZoneId.systemDefault()
): Boolean {
    return instant.toLocalDate(zone) == localDate
}

fun isSameDay(
    instant: Instant,
    localDate: Instant,
    zone: ZoneId = ZoneId.systemDefault()
): Boolean {
    return instant.toLocalDate(zone) == localDate.toLocalDate(zone)
}

fun isSameDay(date1: Date, date2: Date): Boolean {
    val localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    return localDate1 == localDate2
}

data class DateFloatEntry(
    val date: Date,
    val x: Float,
    val y: Float
)


fun List<SolcastForecastResponse>.total(): Double {
    return this.fold(0.0) { total, forecast ->
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
