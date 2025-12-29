package com.alpriest.energystats.shared.models.network

data class SchedulePhaseNetworkModel(
    val enable: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val workMode: String,
    val minSocOnGrid: Int,
    val fdSoc: Int,
    val fdPwr: Int?,
    val maxSoc: Int?
)