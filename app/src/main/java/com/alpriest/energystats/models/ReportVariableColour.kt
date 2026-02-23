package com.alpriest.energystats.models

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.ReportVariable
import com.alpriest.energystats.shared.models.demo
import com.alpriest.energystats.shared.models.isDarkMode
import com.alpriest.energystats.ui.statsgraph.selfSufficiencyLineColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ReportVariable.colour(appSettingsStream: StateFlow<AppSettings>): Color {
    return when (this) {
        ReportVariable.Generation -> Color(243, 178, 116)
        ReportVariable.FeedIn -> Color(181, 121, 223)
        ReportVariable.ChargeEnergyToTal -> Color(125, 208, 130)
        ReportVariable.DischargeEnergyToTal -> Color(80, 147, 248)
        ReportVariable.GridConsumption -> Color(236, 109, 96)
        ReportVariable.Loads -> Color(197, 195, 63)
        ReportVariable.SelfSufficiency -> selfSufficiencyLineColor(isDarkMode(appSettingsStream))
        ReportVariable.PvEnergyToTal -> Color(248, 206, 40)
        ReportVariable.InverterConsumption -> Color(0xFFFF007F)
        ReportVariable.BatterySOC -> Color(116, 208, 235)
    }
}

@Composable
@Preview(showBackground = true)
fun ReportVariableColours() {
    Column {
        ReportVariable.entries.forEach {
            // square of colour
            Row {
                Box(modifier = Modifier.size(16.dp).background(it.colour(MutableStateFlow(AppSettings.demo()))))
                Text(it.name)
            }
        }
    }
}