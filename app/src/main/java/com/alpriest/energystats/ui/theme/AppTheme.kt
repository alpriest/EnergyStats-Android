package com.alpriest.energystats.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGroup
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.DataCeiling
import com.alpriest.energystats.ui.settings.DisplayUnit
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
    val displayUnit: DisplayUnit,
    val showInverterTemperatures: Boolean,
    val showInverterIcon: Boolean,
    val showHomeTotal: Boolean,
    val shouldInvertCT2: Boolean,
    val showGridTotals: Boolean,
    val showInverterTypeNameOnPowerflow: Boolean,
    val showInverterStationNameOnPowerflow: Boolean,
    val showLastUpdateTimestamp: Boolean,
    val solarRangeDefinitions: SolarRangeDefinitions,
    val shouldCombineCT2WithPVPower: Boolean,
    val showGraphValueDescriptions: Boolean,
    var parameterGroups: List<ParameterGroup>,
    val colorTheme: ColorThemeMode,
    val solcastSettings: SolcastSettings,
    val dataCeiling: DataCeiling,
    val totalYieldModel: TotalYieldModel,
    val showFinancialSummaryOnFlowPage: Boolean,
    val separateParameterGraphsByUnit: Boolean,
    val currencySymbol: String,
    val showBatterySOCAsPercentage: Boolean,
    val shouldCombineCT2WithLoadsPower: Boolean
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

    companion object
}

fun AppTheme.Companion.preview(
    useLargeDisplay: Boolean = false,
    useColouredLines: Boolean = true,
    showBatteryTemperature: Boolean = true,
    showBatteryEstimate: Boolean = true,
    showSunnyBackground: Boolean = true,
    decimalPlaces: Int = 2,
    showUsableBatteryOnly: Boolean = false,
    selfSufficiencyEstimateMode: SelfSufficiencyEstimateMode = SelfSufficiencyEstimateMode.Off,
    showFinancialSummary: Boolean = true,
    displayUnit: DisplayUnit = DisplayUnit.Kilowatts,
    showInverterTemperatures: Boolean = false,
    showInverterIcon: Boolean = true,
    showHomeTotal: Boolean = false,
    shouldInvertCT2: Boolean = false,
    showGridTotals: Boolean = false,
    showInverterTypeNameOnPowerflow: Boolean = false,
    showInverterStationNameOnPowerflow: Boolean = false,
    showLastUpdateTimestamp: Boolean = false,
    solarRangeDefinitions: SolarRangeDefinitions = SolarRangeDefinitions.defaults,
    shouldCombineCT2WithPVPower: Boolean = true,
    showGraphValueDescriptions: Boolean = true,
    parameterGroups: List<ParameterGroup> = ParameterGroup.defaults,
    colorTheme: ColorThemeMode = ColorThemeMode.Auto,
    solcastSettings: SolcastSettings = SolcastSettings.defaults,
    dataCeiling: DataCeiling = DataCeiling.Mild,
    totalYieldModel: TotalYieldModel = TotalYieldModel.EnergyStats,
    showFinancialSummaryOnFlowPage: Boolean = true,
    separateParameterGraphsByUnit: Boolean = true,
    currencySymbol: String = "£",
    showBatterySOCAsPercentage: Boolean = false,
    shouldCombineCT2WithLoadsPower: Boolean = false
): AppTheme {
    return AppTheme(
        useLargeDisplay = useLargeDisplay,
        useColouredLines = useColouredLines,
        showBatteryTemperature = showBatteryTemperature,
        showBatteryEstimate = showBatteryEstimate,
        decimalPlaces = decimalPlaces,
        showSunnyBackground = showSunnyBackground,
        showUsableBatteryOnly = showUsableBatteryOnly,
        selfSufficiencyEstimateMode = selfSufficiencyEstimateMode,
        showFinancialSummary = showFinancialSummary,
        displayUnit = displayUnit,
        showInverterTemperatures = showInverterTemperatures,
        showInverterIcon = showInverterIcon,
        showHomeTotal = showHomeTotal,
        shouldInvertCT2 = shouldInvertCT2,
        showGridTotals = showGridTotals,
        showInverterTypeNameOnPowerflow = showInverterTypeNameOnPowerflow,
        showInverterStationNameOnPowerflow = showInverterStationNameOnPowerflow,
        showLastUpdateTimestamp = showLastUpdateTimestamp,
        solarRangeDefinitions = solarRangeDefinitions,
        shouldCombineCT2WithPVPower = shouldCombineCT2WithPVPower,
        showGraphValueDescriptions = showGraphValueDescriptions,
        parameterGroups = parameterGroups,
        colorTheme = colorTheme,
        solcastSettings = solcastSettings,
        dataCeiling = dataCeiling,
        totalYieldModel = totalYieldModel,
        showFinancialSummaryOnFlowPage = showFinancialSummaryOnFlowPage,
        separateParameterGraphsByUnit = separateParameterGraphsByUnit,
        currencySymbol = currencySymbol,
        showBatterySOCAsPercentage = showBatterySOCAsPercentage,
        shouldCombineCT2WithLoadsPower = shouldCombineCT2WithLoadsPower
    )
}