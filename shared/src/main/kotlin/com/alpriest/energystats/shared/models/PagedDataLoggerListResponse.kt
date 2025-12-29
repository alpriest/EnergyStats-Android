package com.alpriest.energystats.shared.models

data class PagedDataLoggerListResponse(
    val total: Int,
    val data: List<DataLoggerResponse>
)

data class DataLoggerResponse(
    val moduleSN: String,
    val stationID: String,
    val status: DataLoggerStatus,
    val signal: Int
)

enum class DataLoggerStatus(val value: Int) {
    UNKNOWN(0),
    ONLINE(1),
    OFFLINE(2)
}

data class DataLoggerListRequest(
    val pageSize: Int = 20,
    val currentPage: Int = 1,
    val total: Int = 0,
    val condition: Condition = Condition()
) {
    data class Condition(
        val communication: Int = 0,
        val moduleSN: String = "",
        val moduleType: String = ""
    )
}
