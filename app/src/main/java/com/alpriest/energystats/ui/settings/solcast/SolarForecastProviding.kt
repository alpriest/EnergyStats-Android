package com.alpriest.energystats.ui.settings.solcast

import com.alpriest.energystats.models.SolcastSiteResponseList

interface SolarForecasting {
    suspend fun fetchSites(apiKey: String): SolcastSiteResponseList
}
