package com.alpriest.energystats.models

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
    val tilt: Int,
    @SerializedName("loss_factor")
    val lossFactor: Double?,
    @SerializedName("dc_capacity")
    val dcCapacity: Double?,
    @SerializedName("install_date")
    val installDate: String?
)
