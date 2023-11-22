package com.alpriest.energystats.ui.settings.solcast

class Solcast : SolarForecasting {
    override fun fetchSites(): List<SolcastSite> {
        return listOf(
            SolcastSite.preview("Front panels"),
            SolcastSite.preview("Rear panels")
        )
    }
}