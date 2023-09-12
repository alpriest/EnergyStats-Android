package com.alpriest.energystats.models

import com.alpriest.energystats.ui.theme.SolarRangeDefinitions

interface ConfigInterface {
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
    var showEstimatedEarnings: Boolean
    var showValuesInWatts: Boolean
    var showInverterTemperatures: Boolean
    var selectedParameterGraphVariables: List<String>
    var showInverterIcon: Boolean
    var showHomeTotal: Boolean
    var shouldInvertCT2: Boolean
    var showGridTotals: Boolean
    var showInverterTypeNameOnPowerflow: Boolean
    var showInverterPlantNameOnPowerflow: Boolean
    var deviceBatteryOverrides: Map<String, String>
    fun clear()
}