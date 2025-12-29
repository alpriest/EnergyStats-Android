package com.alpriest.energystats.shared.models.network

data class ErrorMessagesResponse(
    val messages: MutableMap<String, MutableMap<String, String>>
)