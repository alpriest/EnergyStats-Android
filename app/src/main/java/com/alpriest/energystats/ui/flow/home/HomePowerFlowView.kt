package com.alpriest.energystats.ui.flow.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alpriest.energystats.ui.flow.LineOrientation
import com.alpriest.energystats.ui.flow.PowerFlowView
import com.alpriest.energystats.ui.flow.PowerFlowLinePosition
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGroup
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.DataCeiling
import com.alpriest.energystats.ui.settings.DisplayUnit
import com.alpriest.energystats.ui.settings.SelfSufficiencyEstimateMode
import com.alpriest.energystats.ui.settings.TotalYieldModel
import com.alpriest.energystats.ui.settings.solcast.SolcastSettings
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.EnergyStatsTheme
import com.alpriest.energystats.ui.theme.SolarRangeDefinitions
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun HomePowerFlowView(amount: Double, modifier: Modifier, themeStream: MutableStateFlow<AppTheme>, position: PowerFlowLinePosition = PowerFlowLinePosition.MIDDLE) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(modifier = Modifier.weight(1f)) {
            PowerFlowView(
                amount = amount,
                themeStream = themeStream,
                position = position,
                orientation = LineOrientation.VERTICAL
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePowerFlowViewPreview() {
    EnergyStatsTheme {
        Box(modifier = Modifier.height(300.dp)) {
            HomePowerFlowView(
                amount = 1.0,
                modifier = Modifier,
                themeStream = MutableStateFlow(AppTheme.preview())
            )
        }
    }
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
    showInverterPlantNameOnPowerflow: Boolean = false,
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
    currencySymbol: String = "£"
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
        showInverterPlantNameOnPowerflow = showInverterPlantNameOnPowerflow,
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
        currencySymbol = currencySymbol
    )
}