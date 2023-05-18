package com.alpriest.energystats.models

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

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
            FeedIn -> "Feed-in energy"
            Generation -> "Output energy"
            GridConsumption -> "Grid consumption energy"
            ChargeEnergyToTal -> "Charge energy"
            DischargeEnergyToTal -> "Discharge energy"
        }
    }

    fun description(): String {
        return when (this) {
            FeedIn -> "Power being sent to the grid"
            GridConsumption -> "Power coming from the grid"
            Generation -> "Solar / Battery power coming through the inverter"
            ChargeEnergyToTal -> "Power charging the battery"
            DischargeEnergyToTal -> "Power discharging from the battery"
        }
    }

    fun colour(): Color {
        return when (this) {
            Generation -> Color(248,216,87)
            FeedIn -> Color(105,207,202)
            ChargeEnergyToTal -> Color(125, 208, 130)
            DischargeEnergyToTal -> Color(241, 162, 154)
            GridConsumption -> Color(237, 108, 96)
        }
    }

    companion object
}