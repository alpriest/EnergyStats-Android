package com.alpriest.energystats.models

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.shared.models.md5
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.statsgraph.selfSufficiencyLineColor
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

class Variable(
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
            "loadsPower" -> Color(209, 207, 83)
            Variable.solcastPrediction.variable -> Color.Black
            else ->
                this.variable.md5()?.let {
                    Color(("#" + it.subSequence(0, 6).toString()).toColorInt())
                } ?: Color.Black
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Variable

        return variable == other.variable
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + variable.hashCode()
        result = 31 * result + unit.hashCode()
        return result
    }

    fun fuzzyNameMatches(other: String): Boolean {
        // Systems with multiple batteries can return the raw variable name with a numbering appended
        return variable == other ||
                "${variable}_1" == other ||
                "${variable}_2" == other ||
                "${variable}_3" == other
    }

    companion object
}

val Variable.Companion.solcastPrediction: Variable
    get() = Variable(
        name = "Solcast solar prediction",
        variable = "solcast_prediction",
        unit = "kW"
    )


@Composable
fun ReportVariable.colour(themeStream: MutableStateFlow<AppTheme>): Color {
    return when (this) {
        ReportVariable.Generation -> Color(244, 184, 96)
        ReportVariable.FeedIn -> Color(181, 121, 223)
        ReportVariable.ChargeEnergyToTal -> Color(125, 208, 130)
        ReportVariable.DischargeEnergyToTal -> Color(80, 147, 248)
        ReportVariable.GridConsumption -> Color(236, 109, 96)
        ReportVariable.Loads -> Color(209,207,83)
        ReportVariable.SelfSufficiency -> selfSufficiencyLineColor(isDarkMode(themeStream))
        ReportVariable.PvEnergyToTal -> Color(248, 216, 87)
        ReportVariable.InverterConsumption -> Color(0xFFFF007F)
        ReportVariable.BatterySOC -> Color.Cyan
    }
}
