package com.alpriest.energystats.ui.settings.solcast

interface SolarForecasting {
    fun fetchSites(): List<SolcastSite>
}
