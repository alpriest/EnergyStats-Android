package com.alpriest.energystats.models

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.shared.models.isDarkMode
import com.alpriest.energystats.ui.statsgraph.selfSufficiencyLineColor
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ReportVariable.colour(appSettingsStream: StateFlow<AppSettings>): Color {
    return when (this) {
        ReportVariable.Generation -> Color(244, 184, 96)
        ReportVariable.FeedIn -> Color(181, 121, 223)
        ReportVariable.ChargeEnergyToTal -> Color(125, 208, 130)
        ReportVariable.DischargeEnergyToTal -> Color(80, 147, 248)
        ReportVariable.GridConsumption -> Color(236, 109, 96)
        ReportVariable.Loads -> Color(209, 207, 83)
        ReportVariable.SelfSufficiency -> selfSufficiencyLineColor(isDarkMode(appSettingsStream))
        ReportVariable.PvEnergyToTal -> Color(248, 216, 87)
        ReportVariable.InverterConsumption -> Color(0xFFFF007F)
        ReportVariable.BatterySOC -> Color.Companion.Cyan
    }
}