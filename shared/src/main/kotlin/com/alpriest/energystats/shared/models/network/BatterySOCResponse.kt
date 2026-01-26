package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.Serializable

@Serializable
data class BatterySOCResponse(
    val minSocOnGrid: Int,
    val minSoc: Int
) {
    fun minSocOnGridPercent(): Double {
        return minSocOnGrid.toDouble() / 100.0
    }
}
