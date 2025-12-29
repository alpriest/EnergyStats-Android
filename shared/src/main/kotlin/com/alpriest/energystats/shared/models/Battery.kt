package com.alpriest.energystats.shared.models

data class Battery(
    val capacity: String?,
    val minSOC: String?,
    val hasError: Boolean
)