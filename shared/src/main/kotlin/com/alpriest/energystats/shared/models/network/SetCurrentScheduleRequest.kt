package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.Serializable

@Serializable
data class SetCurrentScheduleRequest(
    val deviceSN: String,
    val groups: List<SchedulePhaseRequest>
)