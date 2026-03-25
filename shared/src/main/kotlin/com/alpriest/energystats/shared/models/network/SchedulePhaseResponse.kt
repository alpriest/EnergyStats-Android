package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.Serializable

@Serializable
data class SchedulePhaseResponse(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val workMode: String,
    val extraParam: Map<String, Double>?
) {
    fun extraParamValue(key: String, default: Int): Int {
        return extraParam?.get(key)?.toInt() ?: default
    }
}

@Serializable
data class SchedulePhaseRequest(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val workMode: String,
    val extraParam: Map<String, Double>?
)