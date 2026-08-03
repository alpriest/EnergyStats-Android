package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.Serializable

@Serializable
data class SetBatterySOCRequest(
    val minSocOnGrid: Int,
    val minSoc: Int,
    val sn: String
)