package com.alpriest.energystats.shared.models

import com.alpriest.energystats.shared.models.network.SolcastSiteResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class SolcastSettings(
    val apiKey: String?,
    val sites: List<SolcastSite>
) {
    companion object {
        val defaults: SolcastSettings = SolcastSettings(apiKey = null, sites = emptyList())
        val demo: SolcastSettings = SolcastSettings(
            apiKey = "123", sites = listOf(SolcastSite.preview())
        )
    }
}

data class SolcastSite(
    val name: String,
    val resourceId: String,
    val lng: Double,
    val lat: Double,
    val azimuth: Int,
    val tilt: Double,
    val lossFactor: Double?,
    val acCapacity: Double,
    val dcCapacity: Double?,
    val installDate: LocalDate?
) {
    constructor(site: SolcastSiteResponse) : this(
        name = site.name,
        resourceId = site.resourceId,
        lng = site.longitude,
        lat = site.latitude,
        azimuth = site.azimuth,
        tilt = site.tilt,
        lossFactor = site.lossFactor,
        acCapacity = site.capacity,
        dcCapacity = site.dcCapacity,
        installDate = site.installDate?.let {
            LocalDate.parse(it.split("T")[0], DateTimeFormatter.ISO_LOCAL_DATE)
        }
    )

    companion object
}

fun SolcastSite.Companion.preview(name: String = "Front panels"): SolcastSite {
    return SolcastSite(
        name = name,
        resourceId = "abc-123-def-456",
        lng = -2.470923,
        lat = 53.377811,
        azimuth = 134,
        tilt = 45.0,
        lossFactor = 0.9,
        acCapacity = 3.7,
        dcCapacity = 5.6,
        installDate = LocalDate.now()
    )
}