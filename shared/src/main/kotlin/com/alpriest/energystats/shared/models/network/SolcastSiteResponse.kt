package com.alpriest.energystats.shared.models.network

import com.google.gson.annotations.SerializedName

data class SolcastSiteResponseList(
    val sites: List<SolcastSiteResponse>
)

data class SolcastSiteResponse(
    val name: String,
    @SerializedName("resource_id")
    val resourceId: String,
    val capacity: Double,
    val longitude: Double,
    val latitude: Double,
    val azimuth: Int,
    val tilt: Double,
    @SerializedName("loss_factor")
    val lossFactor: Double?,
    @SerializedName("dc_capacity")
    val dcCapacity: Double?,
    @SerializedName("install_date")
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