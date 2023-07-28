package com.alpriest.energystats.models

data class AuthRequest(
    val user: String,
    val password: String
)

data class SetSOCRequest(
    val minGridSoc: Int,
    val minSoc: Int,
    val sn: String
)