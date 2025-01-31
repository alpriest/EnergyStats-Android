package com.alpriest.energystats.ui.settings.solcast

import android.content.Context
import com.alpriest.energystats.models.SolcastForecastList
import com.alpriest.energystats.models.SolcastForecastResponse
import com.alpriest.energystats.models.SolcastForecastResponseList
import com.alpriest.energystats.models.SolcastSiteResponseList
import com.alpriest.energystats.services.TryLaterException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

interface SolcastCaching {
    suspend fun fetchSites(apiKey: String): SolcastSiteResponseList
    suspend fun fetchForecast(site: SolcastSite, apiKey: String, ignoreCache: Boolean): SolcastForecastList
}

class SolcastCache(
    private val service: SolarForecasting,
    private val context: Context
) : SolcastCaching {

    override suspend fun fetchSites(apiKey: String): SolcastSiteResponseList {
        return service.fetchSites(apiKey)
    }

    override suspend fun fetchForecast(site: SolcastSite, apiKey: String, ignoreCache: Boolean): SolcastForecastList {
        val cachedData: String? = if (ignoreCache) null else getForecastIfCached(site.resourceId)
        return cachedData?.let {
            val type = object : TypeToken<SolcastForecastResponseList>() {}.type
            val responseList: SolcastForecastResponseList = Gson().fromJson(it, type)
            return SolcastForecastList(tooManyRequests = false, forecasts = responseList.forecasts)
        } ?: fetchAndStore(site, apiKey)
    }

    private suspend fun fetchAndStore(site: SolcastSite, apiKey: String, previous: SolcastForecastResponseList? = null): SolcastForecastList {
        var tooManyRequests = false
        var latest: MutableList<SolcastForecastResponse>

        try {
            latest = service.fetchForecast(site, apiKey).forecasts.toMutableList()
        } catch (ex: TryLaterException) {
            latest = mutableListOf()
            tooManyRequests = true
        }
        val previousForecasts = previous?.forecasts ?: listOf()
        val todayStart = LocalDate.now().atStartOfDay()

        var merged = previousForecasts.map { p ->
            val indexOfLatestForecastPeriod = latest.indexOfFirst { it.periodEnd == p.periodEnd }
            if (indexOfLatestForecastPeriod > -1) {
                latest.removeAt(indexOfLatestForecastPeriod)
            } else {
                p
            }
        }.toMutableList()

        merged.addAll(latest)
        merged = merged.filter { it.periodEnd.toLocalDateTime() >= todayStart }.toMutableList()

        val result = SolcastForecastResponseList(merged)
        val jsonText = Gson().toJson(result)
        saveForecast(site.resourceId, jsonText)

        return SolcastForecastList(tooManyRequests, result.forecasts)
    }

    private fun getForecastIfCached(resourceId: String): String? {
        val file = getFile(resourceId)
        val eightHoursInMillis = 8 * 60 * 60 * 1000
        val currentTime = System.currentTimeMillis()
        if (file.exists() && ((currentTime - file.lastModified()) < eightHoursInMillis)) {
            return file.readText(Charset.defaultCharset())
        }
        return null
    }

    private fun getFile(resourceId: String): File {
        val fileName = "solcast-cache-$resourceId.json"
        return File(context.filesDir, fileName)
    }

    private fun saveForecast(resourceId: String, data: String) {
        val file = getFile(resourceId)
        file.writeText(data, Charset.defaultCharset())
    }
}

fun Date.toLocalDateTime(): LocalDateTime {
    return this.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}