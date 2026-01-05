package com.alpriest.energystats.presentation

import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.shared.models.SolarRangeDefinitions

data class WearPowerFlowState(
    val state: LoadState = LoadState.Inactive,
    val solarAmount: Double,
    val houseLoadAmount: Double,
    val batteryChargeLevel: Double,
    val batteryChargeAmount: Double,
    val gridAmount: Double,
    val solarRangeDefinitions: SolarRangeDefinitions
)