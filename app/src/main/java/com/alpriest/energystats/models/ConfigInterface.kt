package com.alpriest.energystats.models

import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGroup
import com.alpriest.energystats.ui.settings.PowerFlowStringsSet
import com.alpriest.energystats.ui.settings.solcast.SolcastSettings
import com.alpriest.energystats.ui.theme.SolarRangeDefinitions

interface ConfigInterface {
    var showSeparateStringsOnPowerFlow: Boolean
    var shouldCombineCT2WithLoadsPower: Boolean
    var showBatterySOCAsPercentage: Boolean
    var variables: List<Variable>
    var separateParameterGraphsByUnit: Boolean
    var dataCeiling: Int
    var colorTheme: Int
    var showGraphValueDescriptions: Boolean
    var shouldCombineCT2WithPVPower: Boolean
    var currencyCode: String
    var gridImportUnitPrice: Double
    var feedInUnitPrice: Double
    var currencySymbol: String
    var solarRangeDefinitions: SolarRangeDefinitions
    var showLastUpdateTimestamp: Boolean
    var selfSufficiencyEstimateMode: Int
    var showUsableBatteryOnly: Boolean
    var showBatteryEstimate: Boolean
    var showSunnyBackground: Boolean
    var selectedDeviceSN: String?
    var devices: String?
    var refreshFrequency: Int
    var showBatteryTemperature: Boolean
    var useColouredFlowLines: Boolean
    var useLargeDisplay: Boolean
    var isDemoUser: Boolean
    var decimalPlaces: Int
    var showFinancialSummary: Boolean
    var displayUnit: Int
    var showInverterTemperatures: Boolean
    var selectedParameterGraphVariables: List<String>
    var showInverterIcon: Boolean
    var showHomeTotal: Boolean
    var shouldInvertCT2: Boolean
    var showGridTotals: Boolean
    var showInverterTypeNameOnPowerflow: Boolean
    var showInverterStationNameOnPowerflow: Boolean
    var deviceBatteryOverrides: Map<String, String>
    var parameterGroups: List<ParameterGroup>
    var solcastSettings: SolcastSettings
    var totalYieldModel: Int
    var showFinancialSummaryOnFlowPage: Boolean
    var useExperimentalLoadFormula: Boolean
    var enabledPowerFlowStrings: PowerFlowStringsSet

    fun clearDisplaySettings()
    fun clearDeviceSettings()
}