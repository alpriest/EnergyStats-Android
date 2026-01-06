package com.alpriest.energystats.preview

import com.alpriest.energystats.services.WidgetTapAction
import com.alpriest.energystats.shared.models.BatteryData
import com.alpriest.energystats.shared.models.GenerationViewData
import com.alpriest.energystats.shared.models.ParameterGroup
import com.alpriest.energystats.shared.models.PowerFlowStringsSettings
import com.alpriest.energystats.shared.models.PowerStationDetail
import com.alpriest.energystats.shared.models.ScheduleTemplate
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.alpriest.energystats.shared.models.SolcastSettings
import com.alpriest.energystats.shared.models.StoredConfig
import com.alpriest.energystats.shared.models.SummaryDateRange
import com.alpriest.energystats.shared.models.Variable
import com.alpriest.energystats.ui.settings.BatteryTemperatureDisplayMode
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.shared.models.DataCeiling
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.shared.models.SelfSufficiencyEstimateMode
import com.alpriest.energystats.shared.models.TotalYieldModel
import com.alpriest.energystats.ui.settings.financial.EarningsModel
import com.alpriest.energystats.ui.settings.inverter.CT2DisplayMode
import java.time.LocalDateTime

class FakeStoredConfigStore(
    override var scheduleTemplates: List<ScheduleTemplate> = listOf(),
    override var colorTheme: Int = ColorThemeMode.Auto.value,
    override var showGraphValueDescriptions: Boolean = true,
    override var shouldCombineCT2WithPVPower: Boolean = false,
    override var currencyCode: String = "GBP",
    override var feedInUnitPrice: Double = 0.05,
    override var gridImportUnitPrice: Double = 0.15,
    override var isDemoUser: Boolean = true,
    override var useLargeDisplay: Boolean = false,
    override var useColouredFlowLines: Boolean = true,
    override var showBatteryTemperature: Boolean = true,
    override var refreshFrequency: Int = RefreshFrequency.Auto.value,
    override var selectedDeviceSN: String? = null,
    override var devices: String? = "[{\"deviceID\":\"03274209-486c-4ea3-9c28-159f25ee84cb\",\"deviceSN\":\"66BH3720228D004\",\"moduleSN\":\"669W2EFF22FA815\",\"plantName\":\"Alistair Priest\",\"deviceType\":\"H1-3.7-E\",\"country\":\"United Kingdom\",\"countryCode\":\"\",\"feedinDate\":\"2022-02-22 21:46:27 GMT+0000\",\"status\":1,\"power\":3.241,\"generationToday\":8,\"generationTotal\":3723.1,\"productType\":\"H\",\"flowType\":2,\"hasBattery\":true,\"hasPV\":true,\"dataLatestUploadDate\":\"2023-08-04 14:43:36 BST+0100\",\"hasWifiMeter\":false,\"inParallel\":0,\"wifiMeterID\":\"\",\"wifiMeterSN\":\"\",\"atFlag\":false}]",
    override var showSunnyBackground: Boolean = true,
    override var decimalPlaces: Int = 2,
    override var showBatteryEstimate: Boolean = true,
    override var showUsableBatteryOnly: Boolean = false,
    override var selfSufficiencyEstimateMode: Int = SelfSufficiencyEstimateMode.Off.value,
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
    override var powerStationDetail: PowerStationDetail? = null,
    override var showBatteryTimeEstimateOnWidget: Boolean = true,
    override var showSelfSufficiencyStatsGraphOverlay: Boolean = true,
    override var truncatedYAxisOnParameterGraphs: Boolean = false,
    override var earningsModel: Int = EarningsModel.Exported.value,
    override var summaryDateRange: SummaryDateRange = SummaryDateRange.Automatic,
    override var lastSolcastRefresh: LocalDateTime? = null,
    override var widgetTapAction: Int = WidgetTapAction.Launch.value,
    override var batteryTemperatureDisplayMode: Int = BatteryTemperatureDisplayMode.Automatic.value,
    override var showInverterScheduleQuickLink: Boolean = false,
    override var fetchSolcastOnAppLaunch: Boolean = false,
    override var ct2DisplayMode: Int = CT2DisplayMode.Hidden.value,
    override var showStringTotalsAsPercentage: Boolean = false,
    override var generationViewData: GenerationViewData? = null,
    override var showInverterConsumption: Boolean = false,
    override var showBatterySOCOnDailyStats: Boolean = false,
    override var workModes: List<String> = listOf(),
    override var allowNegativeHouseLoad: Boolean = false,
    override var batteryData: BatteryData? = null

) : StoredConfig {
    override fun clearDisplaySettings() {}
    override fun clearDeviceSettings() {}
}