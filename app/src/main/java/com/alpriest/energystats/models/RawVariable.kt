package com.alpriest.energystats.models

import androidx.compose.ui.graphics.Color

class RawVariable(
    val name: String,
    val variable: String,
    val unit: String
) {
    fun colour(): Color {
        return when (this.variable) {
            "generationPower" -> Color(244, 184, 96)
            "feedinPower" -> Color(181, 121, 223)
            "batChargePower" -> Color(125, 208, 130)
            "batDischargePower" -> Color(80, 147, 248)
            "gridConsumptionPower" -> Color(236, 109, 96)
            "loadsPower" -> Color(209,207,83)
            else ->
                this.variable.md5()?.let {
                    Color(android.graphics.Color.parseColor("#" + it.subSequence(0, 6).toString())) } ?: Color.Black
        }
    }
}
