package com.alpriest.energystats.shared.config

import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.BatteryData
import com.alpriest.energystats.shared.models.BatteryTemperatureDisplayMode
import com.alpriest.energystats.shared.models.CT2DisplayMode
import com.alpriest.energystats.shared.models.ColorThemeMode
import com.alpriest.energystats.shared.models.DataCeiling
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.shared.models.DisplayUnit
import com.alpriest.energystats.shared.models.EarningsModel
import com.alpriest.energystats.shared.models.GenerationViewData
import com.alpriest.energystats.shared.models.ParameterGroup
import com.alpriest.energystats.shared.models.PowerFlowStringsSettings
import com.alpriest.energystats.shared.models.PowerStationDetail
import com.alpriest.energystats.shared.models.RefreshFrequency
import com.alpriest.energystats.shared.models.ScheduleTemplate
import com.alpriest.energystats.shared.models.SelfSufficiencyEstimateMode
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.alpriest.energystats.shared.models.SolcastSettings
import com.alpriest.energystats.shared.models.SummaryDateRange
import com.alpriest.energystats.shared.models.TotalYieldModel
import com.alpriest.energystats.shared.models.Variable
import com.alpriest.energystats.shared.models.WidgetTapAction
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

interface StoredConfig {
    var batteryData: BatteryData?
    var workModes: List<String>
    var fetchSolcastOnAppLaunch: Boolean
    var showInverterScheduleQuickLink: Boolean
    var batteryTemperatureDisplayMode: BatteryTemperatureDisplayMode
    var widgetTapAction: WidgetTapAction
    var lastSolcastRefresh: LocalDateTime?
    var scheduleTemplates: List<ScheduleTemplate>
    var showBatteryTimeEstimateOnWidget: Boolean
    var powerStationDetail: PowerStationDetail?
    var showBatterySOCAsPercentage: Boolean
    var variables: List<Variable>
    var separateParameterGraphsByUnit: Boolean
    var dataCeiling: DataCeiling
    var colorTheme: ColorThemeMode
    var showGraphValueDescriptions: Boolean
    var currencyCode: String
    var gridImportUnitPrice: Double
    var feedInUnitPrice: Double
    var currencySymbol: String
    var solarRangeDefinitions: SolarRangeDefinitions
    var showLastUpdateTimestamp: Boolean
    var selfSufficiencyEstimateMode: SelfSufficiencyEstimateMode
    var showBatteryEstimate: Boolean
    var showSunnyBackground: Boolean
    var selectedDeviceSN: String?
    var devices: List<Device>?
    var refreshFrequency: RefreshFrequency
    var showBatteryTemperature: Boolean
    var useColouredFlowLines: Boolean
    var useLargeDisplay: Boolean
    var isDemoUser: Boolean
    var decimalPlaces: Int
    var showFinancialSummary: Boolean
    var displayUnit: DisplayUnit
    var showInverterTemperatures: Boolean
    var selectedParameterGraphVariables: List<String>
    var showInverterIcon: Boolean
    var showHomeTotal: Boolean
    var showInverterTypeNameOnPowerflow: Boolean
    var showInverterStationNameOnPowerflow: Boolean
    var deviceBatteryOverrides: Map<String, String>
    var parameterGroups: List<ParameterGroup>
    var solcastSettings: SolcastSettings
    var totalYieldModel: TotalYieldModel
    var showFinancialSummaryOnFlowPage: Boolean
    var useTraditionalLoadFormula: Boolean
    var showSelfSufficiencyStatsGraphOverlay: Boolean
    var truncatedYAxisOnParameterGraphs: Boolean
    var earningsModel: EarningsModel
    var summaryDateRange: SummaryDateRange
    var ct2DisplayMode: CT2DisplayMode
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
}

interface CurrentStatusCalculatorConfig {
    val appSettingsStream: StateFlow<AppSettings>
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