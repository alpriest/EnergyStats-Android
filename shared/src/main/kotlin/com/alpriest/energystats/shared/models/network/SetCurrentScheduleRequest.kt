package com.alpriest.energystats.shared.models.network

data class SetCurrentScheduleRequest(
    val deviceSN: String,
    val groups: List<SchedulePhaseNetworkModel>
)