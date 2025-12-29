package com.alpriest.energystats.shared.models.network

data class OpenHistoryRequest(
    val sn: String,
    val variables: List<String>,
    val begin: Long,
    val end: Long
)

data class OpenHistoryResponse(
    val deviceSN: String,
    val datas: List<OpenHistoryResponseData>
)

data class OpenHistoryResponseData(
    val unit: String?,
    val name: String,
    val variable: String,
    val data: List<UnitData>
)

data class UnitData(
    val time: String,
    val value: Double
)
