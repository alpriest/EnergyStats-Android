package com.alpriest.energystats.shared.models.network

import OpenRealQueryResponseDeserializer
import kotlinx.serialization.Serializable

data class OpenRealQueryRequest(
    val sns: List<String>,
    val variables: List<String>
)

@Serializable(with = OpenRealQueryResponseDeserializer::class)
data class OpenRealQueryResponse(
    val time: String,
    val deviceSN: String,
    val datas: List<OpenQueryResponseData>
)

@Serializable
data class OpenQueryResponseData(
    val unit: String?,
    val variable: String,
    val value: Double?,
    val valueString: String?
)

fun List<OpenQueryResponseData>.currentValue(forKey: String): Double {
    return currentData(forKey)?.value ?: 0.0
}

fun List<OpenQueryResponseData>.currentData(forKey: String): OpenQueryResponseData? {
    return firstOrNull { it.variable.equals(forKey, ignoreCase = true) }
}

fun List<OpenQueryResponseData>.SoC(): Double {
    return currentData("SoC")?.value ?: currentData("SoC_1")?.value ?: 0.0
}
