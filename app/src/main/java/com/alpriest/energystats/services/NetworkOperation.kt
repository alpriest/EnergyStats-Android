package com.alpriest.energystats.services

import java.time.LocalDateTime

data class NetworkOperation<T>(
    val description: String,
    val value: T,
    val raw: String?,
    val time: LocalDateTime = LocalDateTime.now()
)
