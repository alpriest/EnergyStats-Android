package com.alpriest.energystats.models

import java.time.LocalDate

data class SolcastSiteResponse(
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
)