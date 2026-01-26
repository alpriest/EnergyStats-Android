package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.Serializable

@Serializable
data class ErrorMessagesResponse(
    val messages: MutableMap<String, MutableMap<String, String>>
)