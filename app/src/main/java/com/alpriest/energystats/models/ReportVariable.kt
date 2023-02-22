package com.alpriest.energystats.models

import androidx.compose.ui.graphics.Color

enum class ReportVariable {
    FeedIn,
    Generation,
    GridConsumption,
    ChargeEnergyToTal,
    DischargeEnergyToTal;

    fun networkTitle(): String {
        return when (this) {
            FeedIn -> "feedIn"
            Generation -> "generation"
            GridConsumption -> "gridConsumption"
            ChargeEnergyToTal -> "chargeEnergyToTal"
            DischargeEnergyToTal -> "dischargeEnergyToTal"
        }
    }

    fun title(): String {
        return when (this) {
            FeedIn -> "Feed-in power"
            Generation -> "Output power"
            GridConsumption -> "Grid consumption power"
            ChargeEnergyToTal -> "Charge power"
            DischargeEnergyToTal -> "Discharge power"
        }
    }

    fun description(): String {
        return when (this) {
            FeedIn -> "Power being sent to the grid"
            GridConsumption -> "Power coming from the grid"
            Generation -> "PV / Battery power coming through the inverter"
            ChargeEnergyToTal -> "Power charging the battery"
            DischargeEnergyToTal -> "Power discharging from the battery"
        }
    }

    fun colour(): Color {
        return when (this) {
            Generation -> Color.Yellow
            FeedIn -> Color.Green
            ChargeEnergyToTal -> Color.Green
            DischargeEnergyToTal -> Color.Red
            GridConsumption -> Color.Red
        }
    }
}