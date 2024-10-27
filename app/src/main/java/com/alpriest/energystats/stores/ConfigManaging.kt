package com.alpriest.energystats.stores

import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.PowerStationDetail
import com.alpriest.energystats.models.Variable
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGroup
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.DataCeiling
import com.alpriest.energystats.ui.settings.DisplayUnit
import com.alpriest.energystats.ui.settings.PowerFlowStringsSettings
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.ui.settings.SelfSufficiencyEstimateMode
import com.alpriest.energystats.ui.settings.TotalYieldModel
import com.alpriest.energystats.ui.settings.financial.EarningsModel
import com.alpriest.energystats.ui.settings.inverter.schedule.ScheduleTemplate
import com.alpriest.energystats.ui.settings.solcast.SolcastSettings
import com.alpriest.energystats.ui.summary.SummaryDateRange
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.SolarRangeDefinitions
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime

interface ConfigManaging: ScheduleTemplateConfigManager {
    fun logout(clearDisplaySettings: Boolean, clearDeviceSettings: Boolean)
    suspend fun fetchDevices()
    fun select(device: Device)
    suspend fun fetchPowerStationDetail()

    val themeStream: MutableStateFlow<AppTheme>

    var shouldCombineCT2WithPVPower: Boolean
    var shouldCombineCT2WithLoadsPower: Boolean
    var currencyCode: String
    var gridImportUnitPrice: Double
    var feedInUnitPrice: Double
    var currencySymbol: String
    var parameterGroups: List<ParameterGroup>
    var solarRangeDefinitions: SolarRangeDefinitions
    var showLastUpdateTimestamp: Boolean
    var showInverterTypeNameOnPowerflow: Boolean
    var showInverterStationNameOnPowerflow: Boolean
    var shouldInvertCT2: Boolean
    val variables: List<Variable>
    var showUsableBatteryOnly: Boolean
    var decimalPlaces: Int
    var showFinancialSummary: Boolean
    var showSunnyBackground: Boolean
    var selfSufficiencyEstimateMode: SelfSufficiencyEstimateMode
    var showBatteryEstimate: Boolean
    var showBatteryTemperature: Boolean
    var useLargeDisplay: Boolean
    var displayUnit: DisplayUnit
    var minSOC: Double
    var batteryCapacity: Int
    var isDemoUser: Boolean
    var useColouredFlowLines: Boolean
    var refreshFrequency: RefreshFrequency
    var devices: List<Device>?
    var currentDevice: MutableStateFlow<Device?>
    val selectedDeviceSN: String?
    var appVersion: String
    var showInverterTemperatures: Boolean
    var selectedParameterGraphVariables: List<String>
    var showInverterIcon: Boolean
    var showHomeTotal: Boolean
    var showGridTotals: Boolean
    var showGraphValueDescriptions: Boolean
    var colorThemeMode: ColorThemeMode
    var solcastSettings: SolcastSettings
    var dataCeiling: DataCeiling
    var totalYieldModel: TotalYieldModel
    var showFinancialSummaryOnFlowPage: Boolean
    var separateParameterGraphsByUnit: Boolean
    var showBatteryAsPercentage: Boolean
    var useTraditionalLoadFormula: Boolean
    var powerFlowStrings: PowerFlowStringsSettings
    var powerStationDetail: PowerStationDetail?
    var showBatteryTimeEstimateOnWidget: Boolean
    var showSelfSufficiencyStatsGraphOverlay: Boolean
    var truncatedYAxisOnParameterGraphs: Boolean
    var earningsModel: EarningsModel
    var summaryDateRange: SummaryDateRange
    var lastSolcastRefresh: LocalDateTime?
}

interface ScheduleTemplateConfigManager {
    var scheduleTemplates: List<ScheduleTemplate>
}

