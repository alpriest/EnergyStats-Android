package com.alpriest.energystats.models

import androidx.compose.ui.graphics.Color

enum class RawVariable {
    GenerationPower,
    FeedInPower,
    BatChargePower,
    BatDischargePower,
    GridConsumptionPower,
    LoadsPower;

    fun reportVariable(): ReportVariable? {
        return when (this) {
            GenerationPower -> ReportVariable.Generation
            FeedInPower -> ReportVariable.FeedIn
            BatChargePower -> ReportVariable.ChargeEnergyToTal
            BatDischargePower -> ReportVariable.DischargeEnergyToTal
            GridConsumptionPower -> ReportVariable.GridConsumption
            else -> null
        }
    }

    fun title(): String {
        return when (this) {
            GenerationPower -> "Output power"
            FeedInPower -> "Feed-in power"
            BatChargePower -> "Charge power"
            BatDischargePower -> "Discharge power"
            GridConsumptionPower -> "Grid consumption power"
            LoadsPower -> "Loads power"
        }
    }

    fun colour(): Color {
        return when (this) {
            GenerationPower -> Color.Yellow
            FeedInPower -> Color.Green
            BatChargePower -> Color.Green
            BatDischargePower -> Color.Red
            GridConsumptionPower -> Color.Red
            LoadsPower -> Color.Black
        }
    }

    fun description(): String {
        return when (this) {
            GenerationPower -> "Solar / Battery power coming through the inverter"
            FeedInPower -> "Power being sent to the grid"
            BatChargePower -> "Power charging the battery"
            BatDischargePower -> "Power discharging from the battery"
            GridConsumptionPower -> "Power coming from the grid"
            LoadsPower -> "Loads power"
        }
    }

    fun networkTitle(): String {
        return when (this) {
            FeedInPower -> "feedinPower"
            GenerationPower -> "generationPower"
            BatChargePower -> "batChargePower"
            BatDischargePower -> "batDischargePower"
            GridConsumptionPower -> "gridConsumptionPower"
            LoadsPower -> "loadsPower"
        }
    }
}

