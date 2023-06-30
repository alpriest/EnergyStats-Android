package com.alpriest.energystats.models

import androidx.compose.ui.graphics.Color

enum class ValueUsage {
    SNAPSHOT,
    TOTAL;
}

enum class ReportVariable {
    FeedIn,
    Generation,
    GridConsumption,
    ChargeEnergyToTal,
    DischargeEnergyToTal,
    Loads;

    fun networkTitle(): String {
        return when (this) {
            FeedIn -> "feedin"
            Generation -> "generation"
            GridConsumption -> "gridConsumption"
            ChargeEnergyToTal -> "chargeEnergyToTal"
            DischargeEnergyToTal -> "dischargeEnergyToTal"
            Loads -> "loads"
        }
    }

    fun colour(): Color {
        return when (this) {
            Generation -> Color(244, 184, 96)
            FeedIn -> Color(181, 121, 223)
            ChargeEnergyToTal -> Color(125, 208, 130)
            DischargeEnergyToTal -> Color(80, 147, 248)
            GridConsumption -> Color(236, 109, 96)
            Loads -> Color.Black
        }
    }

    companion object
}

fun ReportVariable.Companion.parse(variable: String): ReportVariable {
    return when (variable.lowercase()) {
        "feedin" -> ReportVariable.FeedIn
        "generation" -> ReportVariable.Generation
        "gridconsumption" -> ReportVariable.GridConsumption
        "chargeenergytotal" -> ReportVariable.ChargeEnergyToTal
        "dischargeenergytotal" -> ReportVariable.DischargeEnergyToTal
        "loads" -> ReportVariable.Loads
        else -> {
            ReportVariable.FeedIn
        }
    }
}
