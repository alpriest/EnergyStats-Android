package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SolcastSiteResponseList(
    val sites: List<SolcastSiteResponse>
)

@Serializable
data class SolcastSiteResponse(
    val name: String,
    @SerialName("resource_id")
    val resourceId: String,
    val capacity: Double,
    val longitude: Double,
    val latitude: Double,
    val azimuth: Int,
    val tilt: Double,
    @SerialName("loss_factor")
    val lossFactor: Double?,
    @SerialName("dc_capacity")
    val dcCapacity: Double?,
    @SerialName("install_date")
    val installDate: String?
)

sealed class SolcastFailure {
    data object TooManyRequests : SolcastFailure()
    data class Unknown(val error: Exception) : SolcastFailure()
}

data class SolcastForecastList(
    var failure: SolcastFailure?,
    var forecasts: List<SolcastForecastResponse>
)