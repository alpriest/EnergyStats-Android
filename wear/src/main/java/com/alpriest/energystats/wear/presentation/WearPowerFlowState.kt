package com.alpriest.energystats.wear.presentation

import com.alpriest.energystats.shared.models.LoadState

data class WearPowerFlowState(
    val state: LoadState = LoadState.Inactive,
    val solarAmount: Double,
    val houseLoadAmount: Double,
    val batteryAmount: Double,
    val gridAmount: Double
)