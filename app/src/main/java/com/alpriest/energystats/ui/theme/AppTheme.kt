package com.alpriest.energystats.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGroup
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.DataCeiling
import com.alpriest.energystats.ui.settings.DisplayUnit
import com.alpriest.energystats.ui.settings.FinancialModel
import com.alpriest.energystats.ui.settings.SelfSufficiencyEstimateMode
import com.alpriest.energystats.ui.settings.TotalYieldModel
import com.alpriest.energystats.ui.settings.solcast.SolcastSettings

data class AppTheme(
    val useLargeDisplay: Boolean,
    val useColouredLines: Boolean,
    val showBatteryTemperature: Boolean,
    val showBatteryEstimate: Boolean,
    val decimalPlaces: Int,
    val showSunnyBackground: Boolean,
    val showUsableBatteryOnly: Boolean,
    val selfSufficiencyEstimateMode: SelfSufficiencyEstimateMode,
    val showFinancialSummary: Boolean,
    val financialModel: FinancialModel,
    val displayUnit: DisplayUnit,
    val showInverterTemperatures: Boolean,
    val showInverterIcon: Boolean,
    val showHomeTotal: Boolean,
    val shouldInvertCT2: Boolean,
    val showGridTotals: Boolean,
    val showInverterTypeNameOnPowerflow: Boolean,
    val showInverterPlantNameOnPowerflow: Boolean,
    val showLastUpdateTimestamp: Boolean,
    val solarRangeDefinitions: SolarRangeDefinitions,
    val shouldCombineCT2WithPVPower: Boolean,
    val showGraphValueDescriptions: Boolean,
    var parameterGroups: List<ParameterGroup>,
    val colorTheme: ColorThemeMode,
    val solcastSettings: SolcastSettings,
    val dataCeiling: DataCeiling,
    val totalYieldModel: TotalYieldModel
) {
    fun fontSize(): TextUnit {
        return when (useLargeDisplay) {
            false -> 16.sp
            true -> 26.sp
        }
    }

    fun smallFontSize(): TextUnit {
        return when (useLargeDisplay) {
            false -> 12.sp
            true -> 22.sp
        }
    }

    fun strokeWidth(): Float {
        return 10f
    }

    fun iconHeight(): Dp {
        return when (useLargeDisplay) {
            false -> 40.dp
            true -> 80.dp
        }
    }

    companion object {}
}
