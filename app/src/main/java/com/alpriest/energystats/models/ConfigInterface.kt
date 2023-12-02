package com.alpriest.energystats.models

import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGroup
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.DataCeiling
import com.alpriest.energystats.ui.settings.FinancialModel
import com.alpriest.energystats.ui.settings.solcast.SolcastSettings
import com.alpriest.energystats.ui.theme.SolarRangeDefinitions

interface ConfigInterface {
    var dataCeiling: Int
    var colorTheme: Int
    var showGraphValueDescriptions: Boolean
    var shouldCombineCT2WithPVPower: Boolean
    var currencyCode: String
    var gridImportUnitPrice: Double
    var feedInUnitPrice: Double
    var currencySymbol: String
    var financialModel: Int
    var solarRangeDefinitions: SolarRangeDefinitions
    var showLastUpdateTimestamp: Boolean
    var selfSufficiencyEstimateMode: Int
    var showUsableBatteryOnly: Boolean
    var showBatteryEstimate: Boolean
    var showSunnyBackground: Boolean
    var selectedDeviceID: String?
    var devices: String?
    var refreshFrequency: Int
    var showBatteryTemperature: Boolean
    var useColouredFlowLines: Boolean
    var useLargeDisplay: Boolean
    var isDemoUser: Boolean
    var decimalPlaces: Int
    var showTotalYield: Boolean
    var showFinancialSummary: Boolean
    var displayUnit: Int
    var showInverterTemperatures: Boolean
    var selectedParameterGraphVariables: List<String>
    var showInverterIcon: Boolean
    var showHomeTotal: Boolean
    var shouldInvertCT2: Boolean
    var showGridTotals: Boolean
    var showInverterTypeNameOnPowerflow: Boolean
    var showInverterPlantNameOnPowerflow: Boolean
    var deviceBatteryOverrides: Map<String, String>
    var parameterGroups: List<ParameterGroup>
    var solcastSettings: SolcastSettings
    fun clear()
}