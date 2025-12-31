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

fun List<OpenQueryResponseData>.currentValue(forKey: String): Double {
    return currentData(forKey)?.value ?: 0.0
}

fun List<OpenQueryResponseData>.currentData(forKey: String): OpenQueryResponseData? {
    return firstOrNull { it.variable.equals(forKey, ignoreCase = true) }
}

fun List<OpenQueryResponseData>.SoC(): Double {
    return currentData("SoC")?.value ?: currentData("SoC_1")?.value ?: 0.0
}
