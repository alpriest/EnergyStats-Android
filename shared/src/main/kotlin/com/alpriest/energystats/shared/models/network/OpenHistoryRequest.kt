package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.Serializable

data class OpenHistoryRequest(
    val sn: String,
    val variables: List<String>,
    val begin: Long,
    val end: Long
)

@Serializable
data class OpenHistoryResponse(
    val deviceSN: String,
    val datas: List<OpenHistoryResponseData>
)

@Serializable
data class OpenHistoryResponseData(
    val unit: String?,
    val name: String,
    val variable: String,
    val data: List<UnitData>
)

@Serializable
data class UnitData(
    val time: String,
    val value: Double
)
