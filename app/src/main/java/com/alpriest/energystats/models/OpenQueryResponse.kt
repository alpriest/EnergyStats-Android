package com.alpriest.energystats.models

data class OpenQueryResponse(
    val time: String,
    val deviceSN: String,
    val datas: List<OpenQueryResponseData>
)

data class OpenQueryResponseData(
    val unit: String,
    val variable: String,
    val value: Double
)

data class OpenHistoryRequest(
    val sn: String,
    val variable: List<Variable>,
    val begin: Double,
    val end: Double
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