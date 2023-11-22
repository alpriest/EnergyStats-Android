package com.alpriest.energystats.models

import java.time.LocalDate

data class SolcastSiteResponse(
    val name: String,
    val resourceId: String,
    val longitude: Double,
    val latitude: Double,
    val azimuth: Int,
    val tilt: Int,
    val lossFactor: Double,
    val capacity: Double,
    val dcCapacity: Double?,
    val installDate: LocalDate?
)