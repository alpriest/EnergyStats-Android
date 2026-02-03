package com.alpriest.energystats.shared.models

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alpriest.energystats.shared.config.StoredConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppSettingsStore(initialValue: AppSettings) {
    private val _appSettingsStream: MutableStateFlow<AppSettings> = MutableStateFlow(initialValue)

    val appSettingStream: StateFlow<AppSettings> = _appSettingsStream
    val currentValue: AppSettings get() = _appSettingsStream.value

    fun update(appSettings: AppSettings) {
        _appSettingsStream.value = appSettings
    }
}

data class AppSettings(
    val useLargeDisplay: Boolean,
    val useColouredFlowLines: Boolean,
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
    val allowNegativeLoad: Boolean,
    val gridImportUnitPrice: Double,
    val feedInUnitPrice: Double,
    val showOutputEnergyOnStats: Boolean
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

fun AppSettings.Companion.demo(
    useLargeDisplay: Boolean = false,
    useColouredFlowLines: Boolean = true,
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
    allowNegativeHouseLoad: Boolean = false,
    showOutputEnergyOnStats: Boolean = false
): AppSettings {
    return AppSettings(
        useLargeDisplay = useLargeDisplay,
        useColouredFlowLines = useColouredFlowLines,
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
        allowNegativeLoad = allowNegativeHouseLoad,
        gridImportUnitPrice = 0.0,
        feedInUnitPrice = 0.0,
        showOutputEnergyOnStats = showOutputEnergyOnStats
    )
}

fun AppSettings.Companion.toAppSettings(config: StoredConfig): AppSettings {
    return AppSettings(
        useLargeDisplay = config.useLargeDisplay,
        useColouredFlowLines = config.useColouredFlowLines,
        showBatteryTemperature = config.showBatteryTemperature,
        showBatteryEstimate = config.showBatteryEstimate,
        decimalPlaces = config.decimalPlaces,
        showSunnyBackground = config.showSunnyBackground,
        showUsableBatteryOnly = config.showUsableBatteryOnly,
        selfSufficiencyEstimateMode = config.selfSufficiencyEstimateMode,
        showFinancialSummary = config.showFinancialSummary,
        displayUnit = config.displayUnit,
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
        colorTheme = config.colorTheme,
        solcastSettings = config.solcastSettings,
        dataCeiling = config.dataCeiling,
        totalYieldModel = config.totalYieldModel,
        showFinancialSummaryOnFlowPage = config.showFinancialSummaryOnFlowPage,
        separateParameterGraphsByUnit = config.separateParameterGraphsByUnit,
        currencySymbol = config.currencySymbol,
        showBatterySOCAsPercentage = config.showBatterySOCAsPercentage,
        shouldCombineCT2WithLoadsPower = config.shouldCombineCT2WithLoadsPower,
        powerFlowStrings = config.powerFlowStrings,
        truncatedYAxisOnParameterGraphs = config.truncatedYAxisOnParameterGraphs,
        showInverterScheduleQuickLink = config.showInverterScheduleQuickLink,
        ct2DisplayMode = config.ct2DisplayMode,
        showStringTotalsAsPercentage = config.showStringTotalsAsPercentage,
        detectedActiveTemplate = config.detectedActiveTemplate,
        showInverterConsumption = config.showInverterConsumption,
        showBatterySOCOnDailyStats = config.showBatterySOCOnDailyStats,
        allowNegativeLoad = config.allowNegativeLoad,
        feedInUnitPrice = config.feedInUnitPrice,
        gridImportUnitPrice = config.gridImportUnitPrice,
        showOutputEnergyOnStats = config.showOutputEnergyOnStats
    )
}