package com.alpriest.energystats.shared.models.network

data class SetBatterySOCRequest(
    val minSocOnGrid: Int,
    val minSoc: Int,
    val sn: String
)