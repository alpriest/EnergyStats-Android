package com.alpriest.energystats.models

data class ErrorMessagesResponse(
    val messages: MutableMap<String, MutableMap<String, String>>
)