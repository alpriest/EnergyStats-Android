package com.alpriest.energystats.presentation

import com.alpriest.energystats.shared.models.LoadState

data class WearPowerFlowState(
    val state: LoadState = LoadState.Inactive,
    val solarAmount: Double,
    val houseLoadAmount: Double,
    val batteryChargeLevel: Double,
    val batteryChargeAmount: Double,
    val gridAmount: Double,
    val threshold1: Double,
    val threshold2: Double,
    val threshold3: Double
)