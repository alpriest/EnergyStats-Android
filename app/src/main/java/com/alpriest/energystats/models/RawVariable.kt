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
            "generationPower" -> Color(248, 216, 87)
            "feedinPower" -> Color(105, 207, 202)
            "batChargePower" -> Color(125, 208, 130)
            "batDischargePower" -> Color(241, 162, 154)
            "gridConsumptionPower" -> Color(237, 108, 96)
            "loadsPower" -> Color.Black
            else ->
                this.variable.md5()?.let { Color(android.graphics.Color.parseColor("#" + it.subSequence(0, 5).toString())) } ?: Color.Black
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
