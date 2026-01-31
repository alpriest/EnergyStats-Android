package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.Serializable

@Serializable
data class PagedPowerStationListResponse(
    val currentPage: Int,
    val pageSize: Int,
    val total: Int,
    val data: List<PowerStationSummaryResponse>
)

@Serializable
data class PowerStationSummaryResponse(val stationID: String)
