package com.alpriest.energystats.ui.settings.solcast

import com.alpriest.energystats.models.SolcastSiteResponse
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGroup
import java.time.LocalDate

data class SolcastSettings(
    val apiKey: String?,
    val sites: List<SolcastSite>
) {
    companion object {
        val defaults: SolcastSettings = SolcastSettings(apiKey = null, sites = listOf())
    }
}

data class SolcastSite(
    val name: String,
    val resourceId: String,
    val lng: Double,
    val lat: Double,
    val azimuth: Int,
    val tilt: Int,
    val lossFactor: Double,
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
        installDate = site.installDate
    )
}
