package com.alpriest.energystats.models

import androidx.compose.ui.graphics.Color

enum class ValueUsage(private val stringValue: String) {
    SNAPSHOT("snapshot"),
    TOTAL("total");
}

enum class ReportVariable {
    FeedIn,
    Generation,
    GridConsumption,
    ChargeEnergyToTal,
    DischargeEnergyToTal;

    fun networkTitle(): String {
        return when (this) {
            FeedIn -> "feedin"
            Generation -> "generation"
            GridConsumption -> "gridConsumption"
            ChargeEnergyToTal -> "chargeEnergyToTal"
            DischargeEnergyToTal -> "dischargeEnergyToTal"
        }
    }

    fun colour(): Color {
        return when (this) {
            Generation -> Color(248, 216, 87)
            FeedIn -> Color(105, 207, 202)
            ChargeEnergyToTal -> Color(125, 208, 130)
            DischargeEnergyToTal -> Color(241, 162, 154)
            GridConsumption -> Color(237, 108, 96)
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
        else -> {
            ReportVariable.FeedIn
        }
    }
}
