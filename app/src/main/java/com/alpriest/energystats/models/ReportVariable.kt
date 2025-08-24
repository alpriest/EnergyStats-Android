package com.alpriest.energystats.models

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.ui.flow.battery.isDarkMode
import com.alpriest.energystats.ui.statsgraph.selfSufficiencyLineColor
import com.alpriest.energystats.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow

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
    Loads,
    SelfSufficiency,
    PvEnergyToTal,
    InverterConsumption,
    BatterySOC;

    fun networkTitle(): String {
        return when (this) {
            FeedIn -> "feedin"
            Generation -> "generation"
            GridConsumption -> "gridConsumption"
            ChargeEnergyToTal -> "chargeEnergyToTal"
            DischargeEnergyToTal -> "dischargeEnergyToTal"
            Loads -> "loads"
            SelfSufficiency -> "selfSufficiency"
            PvEnergyToTal -> "PVEnergyTotal"
            InverterConsumption -> "InverterConsumption"
            BatterySOC -> "BatterySOC"
        }
    }

    @Composable
    fun colour(themeStream: MutableStateFlow<AppTheme>): Color {
        return when (this) {
            Generation -> Color(244, 184, 96)
            FeedIn -> Color(181, 121, 223)
            ChargeEnergyToTal -> Color(125, 208, 130)
            DischargeEnergyToTal -> Color(80, 147, 248)
            GridConsumption -> Color(236, 109, 96)
            Loads -> Color(209,207,83)
            SelfSufficiency -> selfSufficiencyLineColor(isDarkMode(themeStream))
            PvEnergyToTal -> Color(248, 216, 87)
            InverterConsumption -> Color(0xFFFF007F)
            BatterySOC -> Color.Cyan
        }
    }

    companion object
}

fun ReportVariable.Companion.parse(networkVariable: String): ReportVariable {
    return when (networkVariable.lowercase()) {
        "generation" -> ReportVariable.Generation
        "gridconsumption" -> ReportVariable.GridConsumption
        "chargeenergytotal" -> ReportVariable.ChargeEnergyToTal
        "dischargeenergytotal" -> ReportVariable.DischargeEnergyToTal
        "loads" -> ReportVariable.Loads
        "pvenergytotal" -> ReportVariable.PvEnergyToTal
        else -> {
            ReportVariable.FeedIn
        }
    }
}
