package com.alpriest.energystats.ui.settings.solcast

import com.alpriest.energystats.models.SolcastForecastResponseList
import com.alpriest.energystats.models.SolcastSiteResponseList

interface SolarForecasting {
    suspend fun fetchSites(apiKey: String): SolcastSiteResponseList
    suspend fun fetchForecast(site: SolcastSite, apiKey: String): SolcastForecastResponseList
}
