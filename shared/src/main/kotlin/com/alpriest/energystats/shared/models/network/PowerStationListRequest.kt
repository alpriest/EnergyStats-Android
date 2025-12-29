package com.alpriest.energystats.shared.models.network

import com.alpriest.energystats.shared.models.PowerStationDetail

data class PowerStationListRequest(
    val currentPage: Int = 1,
    val pageSize: Int = 100
)

data class PowerStationDetailResponse(
    val stationName: String,
    val capacity: Double,
    val timezone: String
) {
    fun toPowerStationDetail(): PowerStationDetail {
        return PowerStationDetail(
            stationName = stationName,
            capacity = capacity,
            timezone = timezone
        )
    }
}
