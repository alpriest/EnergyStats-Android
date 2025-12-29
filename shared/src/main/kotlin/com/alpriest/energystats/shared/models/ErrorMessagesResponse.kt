package com.alpriest.energystats.shared.models

data class ErrorMessagesResponse(
    val messages: MutableMap<String, MutableMap<String, String>>
)