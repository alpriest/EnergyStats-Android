package com.alpriest.energystats.presentation

import com.alpriest.energystats.shared.models.LoadState
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import java.time.LocalDate

data class WearPowerFlowState(
    val state: LoadState = LoadState.Inactive,
    val lastUpdated: LocalDate,
    val batterySOC: Double?,
    val solarAmount: Double?,
    val houseLoadAmount: Double?,
    val gridAmount: Double?,
    val batteryChargePower: Double?,
    val solarRangeDefinitions: SolarRangeDefinitions,
    val totalExport: Double?,
    val totalImport: Double?
)
