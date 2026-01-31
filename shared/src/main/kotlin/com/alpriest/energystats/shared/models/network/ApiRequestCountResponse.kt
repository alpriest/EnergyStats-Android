package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.Serializable

@Serializable
data class ApiRequestCountResponse(
    val total: Int,
    val remaining: Int
)