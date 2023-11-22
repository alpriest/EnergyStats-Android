package com.alpriest.energystats.ui.settings.solcast

import java.time.LocalDate

class Solcast : SolarForecasting {
    override fun fetchSites(): List<SolcastSite> {
        return listOf(
            SolcastSite(
                name = "Front panels",
                resourceId = "abc-123-def-456",
                lng = -2.470923,
                lat = 53.377811,
                azimuth = 134,
                tilt = 45,
                lossFactor = 0.9,
                acCapacity = 3.7,
                dcCapacity = 5.6,
                installDate = LocalDate.now()
            ),
            SolcastSite(
                name = "Back panels",
                resourceId = "abc-123-def-456",
                lng = -2.470923,
                lat = 53.377811,
                azimuth = 134,
                tilt = 45,
                lossFactor = 0.9,
                acCapacity = 3.7,
                dcCapacity = 5.6,
                installDate = LocalDate.now()
            )
        )
    }
}