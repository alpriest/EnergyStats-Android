package com.alpriest.energystats.services

import okhttp3.Request
import java.time.LocalDateTime

data class NetworkOperation<T>(
    val description: String,
    val value: T,
    val raw: String?,
    val request: Request,
    val time: LocalDateTime = LocalDateTime.now()
)
