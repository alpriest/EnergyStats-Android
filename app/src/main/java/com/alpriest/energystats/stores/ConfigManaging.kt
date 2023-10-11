package com.alpriest.energystats.stores

import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.RawVariable
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGroup
import com.alpriest.energystats.ui.settings.DisplayUnit
import com.alpriest.energystats.ui.settings.FinancialModel
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.ui.settings.SelfSufficiencyEstimateMode
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.SolarRangeDefinitions
import kotlinx.coroutines.flow.MutableStateFlow

interface ConfigManaging {
    fun logout()
    suspend fun fetchDevices()
    suspend fun refreshFirmwareVersions()
    fun select(device: Device)

    var shouldCombineCT2WithPVPower: Boolean
    var currencyCode: String
    var gridImportUnitPrice: Double
    var feedInUnitPrice: Double
    var currencySymbol: String
    var financialModel: FinancialModel
    val parameterGroups: List<ParameterGroup>
    var solarRangeDefinitions: SolarRangeDefinitions
    var showLastUpdateTimestamp: Boolean
    var showInverterTypeNameOnPowerflow: Boolean
    var showInverterPlantNameOnPowerflow: Boolean
    var shouldInvertCT2: Boolean
    val variables: List<RawVariable>
    val hasBattery: Boolean
    var showUsableBatteryOnly: Boolean
    val themeStream: MutableStateFlow<AppTheme>
    var decimalPlaces: Int
    var showTotalYield: Boolean
    var showFinancialSummary: Boolean
    var showSunnyBackground: Boolean
    var selfSufficiencyEstimateMode: SelfSufficiencyEstimateMode
    var showBatteryEstimate: Boolean
    var showBatteryTemperature: Boolean
    var useLargeDisplay: Boolean
    var displayUnit: DisplayUnit
    val minSOC: MutableStateFlow<Double?>
    var batteryCapacity: Int
    var isDemoUser: Boolean
    var useColouredFlowLines: Boolean
    var refreshFrequency: RefreshFrequency
    var devices: List<Device>?
    var currentDevice: MutableStateFlow<Device?>
    val selectedDeviceID: String?
    var appVersion: String
    var showInverterTemperatures: Boolean
    var selectedParameterGraphVariables: List<String>
    var showInverterIcon: Boolean
    var showHomeTotal: Boolean
    var showGridTotals: Boolean
}
