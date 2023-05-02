package com.alpriest.energystats.models

import androidx.compose.ui.graphics.Color

class RawVariable(
    val name: String,
    val variable: String,
    val unit: String
) {
    fun reportVariable(): ReportVariable? {
        return when (this.variable) {
            "generationPower" -> ReportVariable.Generation
            "feedinPower" -> ReportVariable.FeedIn
            "batChargePower" -> ReportVariable.ChargeEnergyToTal
            "batDischargePower" -> ReportVariable.DischargeEnergyToTal
            "gridConsumptionPower" -> ReportVariable.GridConsumption
            else -> null
        }
    }

    fun title(): String {
        return when (this.variable) {
            "generationPower" -> "Output power"
            "feedinPower" -> "Feed-in power"
            "batChargePower" -> "Charge power"
            "batDischargePower" -> "Discharge power"
            "gridConsumptionPower" -> "Grid consumption power"
            "loadsPower" -> "Loads power"
            else -> name
        }
    }

    fun colour(): Color {
        return when (this.variable) {
            "generationPower" -> Color.Yellow
            "feedInPower" -> Color.Green
            "batChargePower" -> Color.Green
            "batDischargePower" -> Color.Red
            "gridConsumptionPower" -> Color.Red
            "loadsPower" -> Color.Black
            else ->
                Color.Black
//                if val md5 = self.variable.md5() {
//                    return Color(hex: String(md5.prefix(6)))
//                } else {
//                    return Color.black
//                }
        }
    }

    fun description(): String {
        return when (this.variable) {
            "generationPower" -> "Solar / Battery power coming through the inverter"
            "feedInPower" -> "Power being sent to the grid"
            "batChargePower" -> "Power charging the battery"
            "batDischargePower" -> "Power discharging from the battery"
            "gridConsumptionPower" -> "Power coming from the grid"
            "loadsPower" -> "Loads power"
            else -> name
        }
    }
}
