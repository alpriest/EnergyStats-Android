package com.alpriest.energystats.ui.summary

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alpriest.energystats.R
import com.alpriest.energystats.models.SolcastFailure
import com.alpriest.energystats.models.SolcastForecastResponse
import com.alpriest.energystats.models.toHalfHourOfDay
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.LoadState
import com.alpriest.energystats.ui.settings.solcast.SolcastCaching
import com.alpriest.energystats.ui.theme.AppTheme
import com.patrykandpatrick.vico.core.entry.ChartEntry
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date

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
    private val themeStream: MutableStateFlow<AppTheme>,
    private val configManager: ConfigManaging
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SolarForecastViewModel(solarForecastProvider, themeStream, configManager) as T
    }
}

class SolarForecastViewModel(
    private val solarForecastProvider: () -> SolcastCaching,
    private val themeStream: MutableStateFlow<AppTheme>,
    private val configManager: ConfigManaging
) : ViewModel() {
    val dataStream = MutableStateFlow<List<SolarForecastViewData>>(listOf())
    var loadStateStream = MutableStateFlow<LoadState>(LoadState.Inactive)
    var tooManyRequestsStream = MutableStateFlow(false)
    var canRefreshStream = MutableStateFlow(true)

    suspend fun load(context: Context, ignoreCache: Boolean = false) {
        updateCanRefresh()
        if (loadStateStream.value != LoadState.Inactive) {
            return
        }
        val settings = themeStream.value.solcastSettings
        if (settings.sites.isEmpty() || settings.apiKey == null) {
            return
        }

        loadStateStream.value = LoadState.Active("Loading...")

        dataStream.value = settings.sites.mapNotNull { site ->
            val forecast = solarForecastProvider().fetchForecast(site, settings.apiKey, ignoreCache)

            if (forecast.failure == null) {
                val forecasts = forecast.forecasts
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
                    today = asGraphData(todayData),
                    todayTotal = total(todayData),
                    tomorrow = asGraphData(tomorrowData),
                    tomorrowTotal = total(tomorrowData),
                    name = site.name,
                    resourceId = site.resourceId
                )
            } else {
                forecast.failure?.let {
                    when (it) {
                        SolcastFailure.TooManyRequests ->
                            loadStateStream.value = LoadState.Error(null, context.getString(R.string.could_not_load_forecast_you_have_exceeded_your_free_daily_limit))

                        is SolcastFailure.Unknown ->
                            loadStateStream.value = LoadState.Error(it.error, context.getString(R.string.unknown_error))
                    }
                }
                null
            }
        }
        loadStateStream.value = LoadState.Inactive
    }

    private fun asGraphData(data: List<SolcastForecastResponse>): List<List<DateFloatEntry>> {
        return listOf(
            data.map { response ->
                DateFloatEntry(
                    date = response.periodEnd,
                    x = response.periodEnd.toHalfHourOfDay().toFloat(),
                    y = response.pvEstimate90.toFloat(),
                )
            },
            data.map { response ->
                DateFloatEntry(
                    date = response.periodEnd,
                    x = response.periodEnd.toHalfHourOfDay().toFloat(),
                    y = response.pvEstimate10.toFloat(),
                )
            },
            data.map { response ->
                DateFloatEntry(
                    date = response.periodEnd,
                    x = response.periodEnd.toHalfHourOfDay().toFloat(),
                    y = response.pvEstimate.toFloat(),
                )
            }
        )
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

    private fun isSameDay(date1: Date, date2: Date): Boolean {
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

    suspend fun refetchSolcast(context: Context) {
        configManager.lastSolcastRefresh = LocalDateTime.now()
        load(context, ignoreCache = true)
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
