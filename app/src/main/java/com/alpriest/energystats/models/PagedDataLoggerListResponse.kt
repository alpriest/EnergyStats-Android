package com.alpriest.energystats.models

data class PagedDataLoggerListResponse(
    val currentPage: Int,
    val pageSize: Int,
    val total: Int,
    val data: List<DataLogger>
) {
    data class DataLogger(
        val moduleSN: String,
        val moduleType: String,
        val plantName: String,
        val version: String,
        val signal: Int,
        val communication: Int
    )
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
