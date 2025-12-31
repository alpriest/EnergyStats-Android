package com.alpriest.energystats.ui.settings.solcast

import com.alpriest.energystats.shared.models.SolcastSite
import com.alpriest.energystats.shared.models.network.SolcastForecastResponseList
import com.alpriest.energystats.shared.models.network.SolcastSiteResponseList

interface SolarForecasting {
    suspend fun fetchSites(apiKey: String): SolcastSiteResponseList
    suspend fun fetchForecast(site: SolcastSite, apiKey: String): SolcastForecastResponseList
}
