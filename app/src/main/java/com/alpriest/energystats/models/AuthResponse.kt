package com.alpriest.energystats.models

data class AuthResponse(
    val token: String,
    val access: Int,
    val user: String
)