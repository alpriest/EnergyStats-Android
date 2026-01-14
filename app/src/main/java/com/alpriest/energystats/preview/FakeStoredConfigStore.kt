package com.alpriest.energystats.preview

import com.alpriest.energystats.shared.config.StoredConfig
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
import java.time.LocalDateTime

class FakeStoredConfigStore(
    override var scheduleTemplates: List<ScheduleTemplate> = listOf(),
    override var colorTheme: ColorThemeMode = ColorThemeMode.Auto,
    override var showGraphValueDescriptions: Boolean = true,
    override var shouldCombineCT2WithPVPower: Boolean = false,
    override var currencyCode: String = "GBP",
    override var feedInUnitPrice: Double = 0.05,
    override var gridImportUnitPrice: Double = 0.15,
    override var isDemoUser: Boolean = true,
    override var useLargeDisplay: Boolean = false,
    override var useColouredFlowLines: Boolean = true,
    override var showBatteryTemperature: Boolean = true,
    override var refreshFrequency: RefreshFrequency = RefreshFrequency.Auto,
    override var selectedDeviceSN: String? = null,
    override var devices: List<Device>? = listOf(
        Device(
            "03274209-486c-4ea3-9c28-159f25ee84cb",
            true,
            "Alistair Priest",
            "Alistair Priest",
            true,
            "H1-3.7-E",
            null,
            moduleSN = "abc123"
        )
    ),
    override var showSunnyBackground: Boolean = true,
    override var decimalPlaces: Int = 2,
    override var showBatteryEstimate: Boolean = true,
    override var showUsableBatteryOnly: Boolean = false,
    override var selfSufficiencyEstimateMode: SelfSufficiencyEstimateMode = SelfSufficiencyEstimateMode.Off,
    override var showFinancialSummary: Boolean = false,
    override var displayUnit: DisplayUnit = DisplayUnit.Adaptive,
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
    override var dataCeiling: DataCeiling = DataCeiling.Mild,
    override var totalYieldModel: TotalYieldModel = TotalYieldModel.EnergyStats,
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
    override var earningsModel: EarningsModel = EarningsModel.Exported,
    override var summaryDateRange: SummaryDateRange = SummaryDateRange.Automatic,
    override var lastSolcastRefresh: LocalDateTime? = null,
    override var widgetTapAction: WidgetTapAction = WidgetTapAction.Launch,
    override var batteryTemperatureDisplayMode: BatteryTemperatureDisplayMode = BatteryTemperatureDisplayMode.Automatic,
    override var showInverterScheduleQuickLink: Boolean = false,
    override var fetchSolcastOnAppLaunch: Boolean = false,
    override var ct2DisplayMode: CT2DisplayMode = CT2DisplayMode.Hidden,
    override var showStringTotalsAsPercentage: Boolean = false,
    override var generationViewData: GenerationViewData? = null,
    override var showInverterConsumption: Boolean = false,
    override var showBatterySOCOnDailyStats: Boolean = false,
    override var workModes: List<String> = listOf(),
    override var allowNegativeLoad: Boolean = false,
    override var batteryData: BatteryData? = null
) : StoredConfig