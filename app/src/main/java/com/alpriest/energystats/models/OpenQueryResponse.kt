package com.alpriest.energystats.models

import java.util.Date

data class OpenQueryResponse(
    val time: Date,
    val deviceSN: String,
    val datas: List<OpenQueryResponseData>
)

data class OpenQueryResponseData(
    val unit: String,
    val variable: String,
    val value: Double
)