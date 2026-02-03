package com.alpriest.energystats.shared.config

import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.BatteryTemperatureDisplayMode
import com.alpriest.energystats.shared.models.CT2DisplayMode
import com.alpriest.energystats.shared.models.ColorThemeMode
import com.alpriest.energystats.shared.models.DataCeiling
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.shared.models.DeviceCapability
import com.alpriest.energystats.shared.models.DisplayUnit
import com.alpriest.energystats.shared.models.EarningsModel
import com.alpriest.energystats.shared.models.ParameterGroup
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
import com.alpriest.energystats.shared.models.WorkMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

interface ConfigManaging: ScheduleTemplateConfigManager, CurrentStatusCalculatorConfig, BatteryConfig {
    fun logout(clearDisplaySettings: Boolean, clearDeviceSettings: Boolean)
    suspend fun fetchDevices()
    fun select(device: Device)
    suspend fun fetchPowerStationDetail()
    var detectedActiveTemplate: String?
    override val appSettingsStream: StateFlow<AppSettings>

    val lastSettingsResetTime: LocalDateTime?
    var currencyCode: String
    var gridImportUnitPrice: Double
    var feedInUnitPrice: Double
    var currencySymbol: String
    var parameterGroups: List<ParameterGroup>
    var solarRangeDefinitions: SolarRangeDefinitions
    var showLastUpdateTimestamp: Boolean
    var showInverterTypeNameOnPowerflow: Boolean
    var showInverterStationNameOnPowerflow: Boolean
    val variables: List<Variable>
    var decimalPlaces: Int
    var showFinancialSummary: Boolean
    var showSunnyBackground: Boolean
    var selfSufficiencyEstimateMode: SelfSufficiencyEstimateMode
    var showBatteryEstimate: Boolean
    var showBatteryTemperature: Boolean
    var useLargeDisplay: Boolean
    var displayUnit: DisplayUnit
    var isDemoUser: Boolean
    var useColouredFlowLines: Boolean
    var refreshFrequency: RefreshFrequency
    var devices: List<Device>
    var currentDevice: MutableStateFlow<Device?>
    val selectedDeviceSN: String?
    var appVersion: String
    var showInverterTemperatures: Boolean
    var selectedParameterGraphVariables: List<String>
    var showInverterIcon: Boolean
    var showHomeTotal: Boolean
    var showGraphValueDescriptions: Boolean
    var colorThemeMode: ColorThemeMode
    var solcastSettings: SolcastSettings
    var dataCeiling: DataCeiling
    var totalYieldModel: TotalYieldModel
    var showFinancialSummaryOnFlowPage: Boolean
    var separateParameterGraphsByUnit: Boolean
    var showBatteryAsPercentage: Boolean
    var useTraditionalLoadFormula: Boolean
    var powerStationDetail: PowerStationDetail?
    var showBatteryTimeEstimateOnWidget: Boolean
    var showSelfSufficiencyStatsGraphOverlay: Boolean
    var truncatedYAxisOnParameterGraphs: Boolean
    var earningsModel: EarningsModel
    var summaryDateRange: SummaryDateRange
    var lastSolcastRefresh: LocalDateTime?
    var widgetTapAction: WidgetTapAction
    var batteryTemperatureDisplayMode: BatteryTemperatureDisplayMode
    var showInverterScheduleQuickLink: Boolean
    var fetchSolcastOnAppLaunch: Boolean
    var ct2DisplayMode: CT2DisplayMode
    fun getDeviceSupports(capability: DeviceCapability, deviceSN: String): Boolean
    fun setDeviceSupports(capability: DeviceCapability, deviceSN: String)
    fun resetDisplaySettings()
    fun loginAsDemo()

    var workModes: List<WorkMode>
    var showInverterConsumption: Boolean
    var showBatterySOCOnDailyStats: Boolean
    var showStringTotalsAsPercentage: Boolean
    var showGridTotals: Boolean
    var showOutputEnergyOnStats: Boolean
}

interface ScheduleTemplateConfigManager {
    var scheduleTemplates: List<ScheduleTemplate>
}