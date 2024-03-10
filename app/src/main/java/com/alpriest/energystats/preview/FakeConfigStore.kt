package com.alpriest.energystats.preview

import com.alpriest.energystats.models.ConfigInterface
import com.alpriest.energystats.models.PowerStationDetail
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGroup
import com.alpriest.energystats.ui.settings.DataCeiling
import com.alpriest.energystats.ui.settings.PowerFlowStringsSettings
import com.alpriest.energystats.ui.settings.TotalYieldModel
import com.alpriest.energystats.ui.settings.solcast.SolcastSettings
import com.alpriest.energystats.ui.theme.SolarRangeDefinitions

class FakeConfigStore(
    override var colorTheme: Int = 0,
    override var showGraphValueDescriptions: Boolean = true,
    override var shouldCombineCT2WithPVPower: Boolean = false,
    override var currencyCode: String = "GBP",
    override var feedInUnitPrice: Double = 0.05,
    override var gridImportUnitPrice: Double = 0.15,
    override var isDemoUser: Boolean = true,
    override var useLargeDisplay: Boolean = false,
    override var useColouredFlowLines: Boolean = true,
    override var showBatteryTemperature: Boolean = true,
    override var refreshFrequency: Int = 0,
    override var selectedDeviceSN: String? = null,
    override var devices: String? = "[{\"deviceID\":\"03274209-486c-4ea3-9c28-159f25ee84cb\",\"deviceSN\":\"66BH3720228D004\",\"moduleSN\":\"669W2EFF22FA815\",\"plantName\":\"Alistair Priest\",\"deviceType\":\"H1-3.7-E\",\"country\":\"United Kingdom\",\"countryCode\":\"\",\"feedinDate\":\"2022-02-22 21:46:27 GMT+0000\",\"status\":1,\"power\":3.241,\"generationToday\":8,\"generationTotal\":3723.1,\"productType\":\"H\",\"flowType\":2,\"hasBattery\":true,\"hasPV\":true,\"dataLatestUploadDate\":\"2023-08-04 14:43:36 BST+0100\",\"hasWifiMeter\":false,\"inParallel\":0,\"wifiMeterID\":\"\",\"wifiMeterSN\":\"\",\"atFlag\":false}]",
    override var showSunnyBackground: Boolean = true,
    override var decimalPlaces: Int = 2,
    override var showBatteryEstimate: Boolean = true,
    override var showUsableBatteryOnly: Boolean = false,
    override var selfSufficiencyEstimateMode: Int = 0,
    override var showFinancialSummary: Boolean = false,
    override var displayUnit: Int = 0,
    override var showInverterTemperatures: Boolean = false,
    override var selectedParameterGraphVariables: List<String> = listOf(),
    override var showInverterIcon: Boolean = false,
    override var showHomeTotal: Boolean = false,
    override var shouldInvertCT2: Boolean = false,
    override var showGridTotals: Boolean = false,
    override var showInverterTypeNameOnPowerflow: Boolean = true,
    override var showInverterStationNameOnPowerflow: Boolean = true,
    override var deviceBatteryOverrides: Map<String, String> = mapOf(),
    override var showLastUpdateTimestamp: Boolean = false,
    override var solarRangeDefinitions: SolarRangeDefinitions = SolarRangeDefinitions.defaults,
    override var parameterGroups: List<ParameterGroup> = listOf(),
    override var currencySymbol: String = "£",
    override var solcastSettings: SolcastSettings = SolcastSettings(apiKey = null, sites = listOf()),
    override var dataCeiling: Int = DataCeiling.Mild.value,
    override var totalYieldModel: Int = TotalYieldModel.EnergyStats.value,
    override var showFinancialSummaryOnFlowPage: Boolean = false,
    override var separateParameterGraphsByUnit: Boolean = true,
    override var variables: List<Variable> = listOf(),
    override var showBatterySOCAsPercentage: Boolean = false,
    override var shouldCombineCT2WithLoadsPower: Boolean = false,
    override var useTraditionalLoadFormula: Boolean = false,
    override var powerFlowStrings: PowerFlowStringsSettings = PowerFlowStringsSettings.defaults,
    override var powerStationDetail: PowerStationDetail? = null

    ) : ConfigInterface {
    override fun clearDisplaySettings() {}
    override fun clearDeviceSettings() {}
}