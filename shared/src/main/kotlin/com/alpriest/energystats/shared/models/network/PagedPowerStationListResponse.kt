package com.alpriest.energystats.shared.models.network

data class PagedPowerStationListResponse(
    val currentPage: Int,
    val pageSize: Int,
    val total: Int,
    val data: List<PowerStationSummaryResponse>
)

data class PowerStationSummaryResponse(val stationID: String)
