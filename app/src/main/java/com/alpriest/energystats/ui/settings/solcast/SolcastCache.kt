package com.alpriest.energystats.ui.settings.solcast

import android.content.Context
import com.alpriest.energystats.models.SolcastFailure
import com.alpriest.energystats.models.SolcastForecastList
import com.alpriest.energystats.models.SolcastForecastResponse
import com.alpriest.energystats.models.SolcastForecastResponseList
import com.alpriest.energystats.models.SolcastSiteResponseList
import com.alpriest.energystats.services.TryLaterException
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

interface SolcastCaching {
    suspend fun fetchSites(apiKey: String): SolcastSiteResponseList
    suspend fun fetchForecast(site: SolcastSite, apiKey: String, ignoreCache: Boolean): SolcastForecastList
}

class SolcastCache(
    private val service: SolarForecasting,
    private val context: Context
) : SolcastCaching {

    private val sitesLock = Mutex()
    private val siteLocks = ConcurrentHashMap<String, Mutex>()

    private fun lockFor(resourceId: String): Mutex = siteLocks.getOrPut(resourceId) { Mutex() }

    override suspend fun fetchSites(apiKey: String): SolcastSiteResponseList {
        return sitesLock.withLock {
            service.fetchSites(apiKey)
        }
    }

    override suspend fun fetchForecast(site: SolcastSite, apiKey: String, ignoreCache: Boolean): SolcastForecastList {
        return lockFor(site.resourceId).withLock {
            getCachedData(site.resourceId)?.let { it ->
                val type = object : TypeToken<SolcastForecastResponseList>() {}.type
                val cachedResponseList: SolcastForecastResponseList = Gson().fromJson(it, type)

                val eightHoursInMillis = 8 * 60 * 60 * 1000
                val currentTime = System.currentTimeMillis()
                val file = getFile(site.resourceId)
                if ((currentTime - file.lastModified()) > eightHoursInMillis || ignoreCache) {
                    // Fetch new data
                    fetchAndStore(site, apiKey, previous = cachedResponseList)
                } else {
                    // Return cached data
                    SolcastForecastList(failure = null, forecasts = cachedResponseList.forecasts)
                }
            } ?: fetchAndStore(site, apiKey)
        }
    }

    private suspend fun fetchAndStore(site: SolcastSite, apiKey: String, previous: SolcastForecastResponseList? = null): SolcastForecastList {
        var latest: MutableList<SolcastForecastResponse>
        var failure: SolcastFailure? = null

        try {
            latest = service.fetchForecast(site, apiKey).forecasts.toMutableList()
        } catch (ex: TryLaterException) {
            latest = mutableListOf()
            failure = SolcastFailure.TooManyRequests
        } catch (ex: Exception) {
            latest = mutableListOf()
            failure = SolcastFailure.Unknown(ex)
        }
        val previousForecasts = previous?.forecasts ?: listOf()
        val oldestCacheData = LocalDate.now().minusDays(7).atStartOfDay()

        // Remove periods duplicated in the cached version and newly fetched data
        var merged = previousForecasts.map { p ->
            val indexOfLatestForecastPeriod = latest.indexOfFirst { it.periodEnd == p.periodEnd }
            if (indexOfLatestForecastPeriod > -1) {
                latest.removeAt(indexOfLatestForecastPeriod)
            } else {
                p
            }
        }.toMutableList()

        merged.addAll(latest)
        merged = merged.filter { it.periodEnd.toLocalDateTime() >= oldestCacheData }.toMutableList()

        val result = SolcastForecastResponseList(merged)
        val jsonText = Gson().toJson(result)
        unsafe_saveForecast(site.resourceId, jsonText)

        return SolcastForecastList(failure, result.forecasts)
    }

    private fun getCachedData(resourceId: String): String? {
        val file = getFile(resourceId)
        if (file.exists()) {
            return file.readText(Charset.defaultCharset())
        }
        return null
    }

    private fun getFile(resourceId: String): File {
        val fileName = "solcast-cache-$resourceId.json"
        return File(context.filesDir, fileName)
    }

    private fun unsafe_saveForecast(resourceId: String, data: String) {
        val file = getFile(resourceId)
        file.writeText(data, Charset.defaultCharset())
    }
}

fun Date.toLocalDateTime(): LocalDateTime {
    return this.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}