package com.alpriest.energystats.shared.models.network

data class OpenRealQueryRequest(
    val deviceSN: String,
    val variables: List<String>
)

data class OpenRealQueryResponse(
    val time: String,
    val deviceSN: String,
    val datas: List<OpenQueryResponseData>
)

data class OpenQueryResponseData(
    val unit: String?,
    val variable: String,
    val value: Double?,
    val valueString: String?
)
