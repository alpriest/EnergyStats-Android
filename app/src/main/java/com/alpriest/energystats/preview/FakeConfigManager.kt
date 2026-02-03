package com.alpriest.energystats.preview

import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.Battery
import com.alpriest.energystats.shared.models.BatteryTemperatureDisplayMode
import com.alpriest.energystats.shared.models.CT2DisplayMode
import com.alpriest.energystats.shared.models.ColorThemeMode
import com.alpriest.energystats.shared.models.DataCeiling
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.shared.models.DeviceCapability
import com.alpriest.energystats.shared.models.DisplayUnit
import com.alpriest.energystats.shared.models.EarningsModel
import com.alpriest.energystats.shared.models.ParameterGroup
import com.alpriest.energystats.shared.models.PowerFlowStringsSettings
import com.alpriest.energystats.shared.models.PowerStationDetail
import com.alpriest.energystats.shared.models.RefreshFrequency
import com.alpriest.energystats.shared.models.ScheduleTemplate
import com.alpriest.energystats.shared.models.SelfSufficiencyEstimateMode
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.alpriest.energystats.shared.models.SolcastSettings
import com.alpriest.energystats.shared.models.SolcastSite
import com.alpriest.energystats.shared.models.SummaryDateRange
import com.alpriest.energystats.shared.models.TotalYieldModel
import com.alpriest.energystats.shared.models.Variable
import com.alpriest.energystats.shared.models.WidgetTapAction
import com.alpriest.energystats.shared.models.demo
import com.alpriest.energystats.shared.models.preview
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime

class FakeConfigManager : ConfigManaging {
    override var detectedActiveTemplate: String? = null
    override var showInverterConsumption: Boolean = false
    override var colorThemeMode: ColorThemeMode = ColorThemeMode.Auto
    override var showGraphValueDescriptions: Boolean = true
    override var shouldCombineCT2WithPVPower: Boolean = true
    override var shouldCombineCT2WithLoadsPower: Boolean = false
    override var currencyCode: String = "GBP"
    override var feedInUnitPrice: Double = 0.05
    override var gridImportUnitPrice: Double = 0.15
    override var currencySymbol: String = "£"
    override val variables: List<Variable> = listOf()
    override var showUsableBatteryOnly: Boolean = false
    override val appSettingsStream: MutableStateFlow<AppSettings> = MutableStateFlow(AppSettings.demo())
    override var decimalPlaces: Int = 3
    override var showSunnyBackground: Boolean = true
    override var selfSufficiencyEstimateMode: SelfSufficiencyEstimateMode = SelfSufficiencyEstimateMode.Off
    override var showBatteryEstimate: Boolean = false
    override var showBatteryTemperature: Boolean = false
    override var showFinancialSummary: Boolean = true
    override var useLargeDisplay: Boolean = false
    override var minSOC: Double = 20.0
    override var batteryCapacity: String = "3000"
    override val batteryCapacityW: Int = batteryCapacity.toInt()
    override var isDemoUser: Boolean = true
    override var useColouredFlowLines: Boolean = true
    override var refreshFrequency: RefreshFrequency = RefreshFrequency.Auto
    override var displayUnit = DisplayUnit.Kilowatts
    override var showInverterTemperatures: Boolean = true
    override var selectedParameterGraphVariables: List<String> = listOf()
    override var showInverterIcon: Boolean = true
    override var showHomeTotal: Boolean = true
    override var shouldInvertCT2: Boolean = false
    override var showGridTotals: Boolean = false
    override var showInverterStationNameOnPowerflow: Boolean = false
    override var showInverterTypeNameOnPowerflow: Boolean = false
    override var showLastUpdateTimestamp: Boolean = false
    override var solarRangeDefinitions: SolarRangeDefinitions = SolarRangeDefinitions.defaults
    override var parameterGroups: List<ParameterGroup> = ParameterGroup.defaults
    override var solcastSettings: SolcastSettings = SolcastSettings(apiKey = null, sites = listOf(SolcastSite.preview()))
    override var dataCeiling: DataCeiling = DataCeiling.Mild
    override var totalYieldModel: TotalYieldModel = TotalYieldModel.Off
    override var showFinancialSummaryOnFlowPage: Boolean = false
    override var separateParameterGraphsByUnit: Boolean = true
    override var showBatteryAsPercentage: Boolean = false
    override var useTraditionalLoadFormula: Boolean = false
    override var powerFlowStrings: PowerFlowStringsSettings = PowerFlowStringsSettings.defaults
    override var powerStationDetail: PowerStationDetail? = null
    override var showBatteryTimeEstimateOnWidget: Boolean = true
    override var showSelfSufficiencyStatsGraphOverlay: Boolean = true
    override var scheduleTemplates: List<ScheduleTemplate> = listOf()
    override var truncatedYAxisOnParameterGraphs: Boolean = true
    override var earningsModel: EarningsModel = EarningsModel.Exported
    override var summaryDateRange: SummaryDateRange = SummaryDateRange.Automatic
    override var lastSolcastRefresh: LocalDateTime? = null
    override var widgetTapAction: WidgetTapAction = WidgetTapAction.Launch
    override var batteryTemperatureDisplayMode: BatteryTemperatureDisplayMode = BatteryTemperatureDisplayMode.Automatic
    override var showInverterScheduleQuickLink: Boolean = false
    override var fetchSolcastOnAppLaunch: Boolean = false
    override var ct2DisplayMode: CT2DisplayMode = CT2DisplayMode.Hidden
    override fun getDeviceSupports(capability: DeviceCapability, deviceSN: String): Boolean {
        return false
    }

    override fun setDeviceSupports(capability: DeviceCapability, deviceSN: String) {}
    override var showStringTotalsAsPercentage: Boolean = false
    override var showBatterySOCOnDailyStats: Boolean = false
    override var allowNegativeLoad: Boolean = false
    override fun loginAsDemo() {}
    override var showOutputEnergyOnStats: Boolean = false

    override var devices: List<Device> = listOf(
        Device(
            deviceSN = "123123",
            stationName = "my station",
            stationID = "station1",
            battery = Battery("1200", "20", false),
            moduleSN = "module123",
            hasPV = true,
            hasBattery = true,
            deviceType = "F1-3000"
        ),
        Device(
            deviceSN = "123123",
            stationName = "my station",
            stationID = "station2",
            battery = Battery("1200", "20", false),
            moduleSN = "module123",
            hasPV = true,
            hasBattery = true,
            deviceType = "H1-5A"
        )
    )
    override var currentDevice: MutableStateFlow<Device?> = MutableStateFlow(
        Device(
            deviceSN = "123123",
            stationName = "my station",
            stationID = "station1",
            battery = Battery("1200", "20", false),
            moduleSN = "module123",
            hasPV = true,
            hasBattery = true,
            deviceType = "F1-3000"
        )
    )
    override val selectedDeviceSN: String? = "f3000_deviceid"

    override fun logout(clearDisplaySettings: Boolean, clearDeviceSettings: Boolean) {
    }

    override suspend fun fetchDevices() {
    }

    override fun select(device: Device) {
    }

    override suspend fun fetchPowerStationDetail() {
    }

    override val lastSettingsResetTime: LocalDateTime? = null

    override var appVersion: String = "1.29"

    override fun resetDisplaySettings() {}

    override var workModes: List<String> = listOf()
}