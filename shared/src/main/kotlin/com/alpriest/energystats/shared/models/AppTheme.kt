package com.alpriest.energystats.shared.models

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.shared.config.StoredConfig

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
    val shouldCombineCT2WithLoadsPower: Boolean,
    val powerFlowStrings: PowerFlowStringsSettings,
    val truncatedYAxisOnParameterGraphs: Boolean,
    val showInverterScheduleQuickLink: Boolean,
    val ct2DisplayMode: CT2DisplayMode,
    val showStringTotalsAsPercentage: Boolean,
    val detectedActiveTemplate: String?,
    val showInverterConsumption: Boolean,
    val showBatterySOCOnDailyStats: Boolean,
    val allowNegativeHouseLoad: Boolean
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
            true -> 60.dp
            false -> 40.dp
        }
    }

    companion object
}

fun AppTheme.Companion.demo(
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
    solcastSettings: SolcastSettings = SolcastSettings.demo,
    dataCeiling: DataCeiling = DataCeiling.Mild,
    totalYieldModel: TotalYieldModel = TotalYieldModel.EnergyStats,
    showFinancialSummaryOnFlowPage: Boolean = true,
    separateParameterGraphsByUnit: Boolean = true,
    currencySymbol: String = "£",
    showBatterySOCAsPercentage: Boolean = false,
    shouldCombineCT2WithLoadsPower: Boolean = false,
    powerFlowStrings: PowerFlowStringsSettings = PowerFlowStringsSettings.defaults.copy(enabled = true, pv1Enabled = true, pv2Enabled = true),
    truncatedYAxisOnParameterGraphs: Boolean = false,
    showInverterScheduleQuickLink: Boolean = true,
    ct2DisplayMode: CT2DisplayMode = CT2DisplayMode.Hidden,
    showStringTotalsAsPercentage: Boolean = false,
    detectedActiveTemplate: String? = "Football",
    showInverterConsumption: Boolean = false,
    showBatterySOCOnDailyStats: Boolean = false,
    allowNegativeHouseLoad: Boolean = false
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
        shouldCombineCT2WithLoadsPower = shouldCombineCT2WithLoadsPower,
        powerFlowStrings = powerFlowStrings,
        truncatedYAxisOnParameterGraphs = truncatedYAxisOnParameterGraphs,
        showInverterScheduleQuickLink = showInverterScheduleQuickLink,
        ct2DisplayMode = ct2DisplayMode,
        showStringTotalsAsPercentage = showStringTotalsAsPercentage,
        detectedActiveTemplate = detectedActiveTemplate,
        showInverterConsumption = showInverterConsumption,
        showBatterySOCOnDailyStats = showBatterySOCOnDailyStats,
        allowNegativeHouseLoad = allowNegativeHouseLoad
    )
}

fun AppTheme.Companion.toAppTheme(config: StoredConfig): AppTheme {
    return AppTheme(
        useLargeDisplay = config.useLargeDisplay,
        useColouredLines = config.useColouredFlowLines,
        showBatteryTemperature = config.showBatteryTemperature,
        showBatteryEstimate = config.showBatteryEstimate,
        decimalPlaces = config.decimalPlaces,
        showSunnyBackground = config.showSunnyBackground,
        showUsableBatteryOnly = config.showUsableBatteryOnly,
        selfSufficiencyEstimateMode = SelfSufficiencyEstimateMode.fromInt(config.selfSufficiencyEstimateMode),
        showFinancialSummary = config.showFinancialSummary,
        displayUnit = DisplayUnit.fromInt(config.displayUnit),
        showInverterTemperatures = config.showInverterTemperatures,
        showInverterIcon = config.showInverterIcon,
        showHomeTotal = config.showHomeTotal,
        shouldInvertCT2 = config.shouldInvertCT2,
        showGridTotals = config.showGridTotals,
        showInverterTypeNameOnPowerflow = config.showInverterTypeNameOnPowerflow,
        showInverterStationNameOnPowerflow = config.showInverterStationNameOnPowerflow,
        showLastUpdateTimestamp = config.showLastUpdateTimestamp,
        solarRangeDefinitions = config.solarRangeDefinitions,
        shouldCombineCT2WithPVPower = config.shouldCombineCT2WithPVPower,
        showGraphValueDescriptions = config.showGraphValueDescriptions,
        parameterGroups = config.parameterGroups,
        colorTheme = ColorThemeMode.fromInt(config.colorTheme),
        solcastSettings = config.solcastSettings,
        dataCeiling = DataCeiling.fromInt(config.dataCeiling),
        totalYieldModel = TotalYieldModel.fromInt(config.totalYieldModel),
        showFinancialSummaryOnFlowPage = config.showFinancialSummaryOnFlowPage,
        separateParameterGraphsByUnit = config.separateParameterGraphsByUnit,
        currencySymbol = config.currencySymbol,
        showBatterySOCAsPercentage = config.showBatterySOCAsPercentage,
        shouldCombineCT2WithLoadsPower = config.shouldCombineCT2WithLoadsPower,
        powerFlowStrings = config.powerFlowStrings,
        truncatedYAxisOnParameterGraphs = config.truncatedYAxisOnParameterGraphs,
        showInverterScheduleQuickLink = config.showInverterScheduleQuickLink,
        ct2DisplayMode = CT2DisplayMode.fromInt(config.ct2DisplayMode),
        showStringTotalsAsPercentage = config.showStringTotalsAsPercentage,
        detectedActiveTemplate = null,
        showInverterConsumption = config.showInverterConsumption,
        showBatterySOCOnDailyStats = config.showBatterySOCOnDailyStats,
        allowNegativeHouseLoad = config.allowNegativeHouseLoad
    )
}