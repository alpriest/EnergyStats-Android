package com.alpriest.energystats.shared.config

import com.alpriest.energystats.shared.models.AppTheme
import com.alpriest.energystats.shared.models.BatteryData
import com.alpriest.energystats.shared.models.GenerationViewData
import com.alpriest.energystats.shared.models.ParameterGroup
import com.alpriest.energystats.shared.models.PowerFlowStringsSettings
import com.alpriest.energystats.shared.models.PowerStationDetail
import com.alpriest.energystats.shared.models.ScheduleTemplate
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.alpriest.energystats.shared.models.SolcastSettings
import com.alpriest.energystats.shared.models.SummaryDateRange
import com.alpriest.energystats.shared.models.Variable
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime

interface StoredConfig {
    var batteryData: BatteryData?
    var workModes: List<String>
    var fetchSolcastOnAppLaunch: Boolean
    var showInverterScheduleQuickLink: Boolean
    var batteryTemperatureDisplayMode: Int
    var widgetTapAction: Int
    var lastSolcastRefresh: LocalDateTime?
    var scheduleTemplates: List<ScheduleTemplate>
    var showBatteryTimeEstimateOnWidget: Boolean
    var powerStationDetail: PowerStationDetail?
    var showBatterySOCAsPercentage: Boolean
    var variables: List<Variable>
    var separateParameterGraphsByUnit: Boolean
    var dataCeiling: Int
    var colorTheme: Int
    var showGraphValueDescriptions: Boolean
    var currencyCode: String
    var gridImportUnitPrice: Double
    var feedInUnitPrice: Double
    var currencySymbol: String
    var solarRangeDefinitions: SolarRangeDefinitions
    var showLastUpdateTimestamp: Boolean
    var selfSufficiencyEstimateMode: Int
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
    var showInverterTypeNameOnPowerflow: Boolean
    var showInverterStationNameOnPowerflow: Boolean
    var deviceBatteryOverrides: Map<String, String>
    var parameterGroups: List<ParameterGroup>
    var solcastSettings: SolcastSettings
    var totalYieldModel: Int
    var showFinancialSummaryOnFlowPage: Boolean
    var useTraditionalLoadFormula: Boolean
    var showSelfSufficiencyStatsGraphOverlay: Boolean
    var truncatedYAxisOnParameterGraphs: Boolean
    var earningsModel: Int
    var summaryDateRange: SummaryDateRange
    var ct2DisplayMode: Int
    var showStringTotalsAsPercentage: Boolean
    var generationViewData: GenerationViewData?
    var showInverterConsumption: Boolean
    var showBatterySOCOnDailyStats: Boolean
    var showUsableBatteryOnly: Boolean
    var showGridTotals: Boolean
    var shouldInvertCT2: Boolean
    var shouldCombineCT2WithPVPower: Boolean
    var powerFlowStrings: PowerFlowStringsSettings
    var shouldCombineCT2WithLoadsPower: Boolean
    var allowNegativeLoad: Boolean

    fun clearDisplaySettings()
    fun clearDeviceSettings()
}

interface CurrentStatusCalculatorConfig {
    val themeStream: MutableStateFlow<AppTheme>
    var shouldInvertCT2: Boolean
    var shouldCombineCT2WithPVPower: Boolean
    var powerFlowStrings: PowerFlowStringsSettings
    var shouldCombineCT2WithLoadsPower: Boolean
    var allowNegativeLoad: Boolean
}

interface BatteryConfig {
    var batteryCapacity: String
    val batteryCapacityW: Int
    var minSOC: Double
    var showUsableBatteryOnly: Boolean
}