package com.alpriest.energystats.ui.login

import com.alpriest.energystats.shared.config.ConfigManaging
import com.alpriest.energystats.shared.models.AppSettings
import com.alpriest.energystats.shared.models.AppSettingsStore
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
import com.alpriest.energystats.shared.models.SummaryDateRange
import com.alpriest.energystats.shared.models.TotalYieldModel
import com.alpriest.energystats.shared.models.Variable
import com.alpriest.energystats.shared.models.WidgetTapAction
import com.alpriest.energystats.shared.models.demo
import com.alpriest.energystats.shared.models.toAppSettings
import com.alpriest.energystats.shared.network.InvalidTokenException
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.shared.ui.roundedToString
import com.alpriest.energystats.stores.StoredConfigManaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import java.net.SocketTimeoutException
import java.time.LocalDateTime
import java.util.Locale

open class ConfigManager(var config: StoredConfigManaging, val networking: Networking, override var appVersion: String, private val appSettingsStore: AppSettingsStore) :
    ConfigManaging {
    private var deviceSupportsScheduleMaxSOC: MutableMap<String, Boolean> = mutableMapOf() // In-memory only
    private var deviceSupportsPeakShaving: MutableMap<String, Boolean> = mutableMapOf() // In-memory only
    override var lastSettingsResetTime: LocalDateTime? = null
    override val appSettingsStream: StateFlow<AppSettings>
        get() = appSettingsStore.appSettingStream

    override var detectedActiveTemplate: String?
        get() = appSettingsStream.value.detectedActiveTemplate
        set(value) {
            config.detectedActiveTemplate = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showBatteryTimeEstimateOnWidget: Boolean
        get() = config.showBatteryTimeEstimateOnWidget
        set(value) {
            config.showBatteryTimeEstimateOnWidget = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var useTraditionalLoadFormula: Boolean
        get() = config.useTraditionalLoadFormula
        set(value) {
            config.useTraditionalLoadFormula = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var colorThemeMode: ColorThemeMode
        get() = config.colorTheme
        set(value) {
            config.colorTheme = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showGraphValueDescriptions: Boolean
        get() = config.showGraphValueDescriptions
        set(value) {
            config.showGraphValueDescriptions = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var shouldCombineCT2WithPVPower: Boolean
        get() = config.shouldCombineCT2WithPVPower
        set(value) {
            config.shouldCombineCT2WithPVPower = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var shouldCombineCT2WithLoadsPower: Boolean
        get() = config.shouldCombineCT2WithLoadsPower
        set(value) {
            config.shouldCombineCT2WithLoadsPower = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var currencyCode: String
        get() = config.currencyCode
        set(value) {
            config.currencyCode = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var currencySymbol: String
        get() = config.currencySymbol
        set(value) {
            config.currencySymbol = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var gridImportUnitPrice: Double
        get() = config.gridImportUnitPrice
        set(value) {
            config.gridImportUnitPrice = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var feedInUnitPrice: Double
        get() = config.feedInUnitPrice
        set(value) {
            config.feedInUnitPrice = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var decimalPlaces: Int
        get() = config.decimalPlaces
        set(value) {
            config.decimalPlaces = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showSunnyBackground: Boolean
        get() = config.showSunnyBackground
        set(value) {
            config.showSunnyBackground = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var selfSufficiencyEstimateMode: SelfSufficiencyEstimateMode
        get() = config.selfSufficiencyEstimateMode
        set(value) {
            config.selfSufficiencyEstimateMode = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showFinancialSummary: Boolean
        get() = config.showFinancialSummary
        set(value) {
            config.showFinancialSummary = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showBatteryEstimate: Boolean
        get() = config.showBatteryEstimate
        set(value) {
            config.showBatteryEstimate = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var displayUnit: DisplayUnit
        get() = config.displayUnit
        set(value) {
            config.displayUnit = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var minSOC: Double
        get() {
            return currentDevice.value?.battery?.let {
                it.minSOC?.toDouble() ?: 0.0
            } ?: 0.0
        }
        set(value) {
            currentDevice.value?.let { device ->
                device.battery?.let { battery ->
                    val updatedDevice = device.copy(battery = battery.copy(minSOC = value.roundedToString(decimalPlaces = 2, locale = Locale.UK)))
                    devices = devices.map {
                        if (it.deviceSN == updatedDevice.deviceSN) updatedDevice else it
                    }
                }
            }
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var batteryCapacity: String
        get() {
            return currentDevice.value?.let {
                val override = config.deviceBatteryOverrides[it.deviceSN]
                return (override ?: it.battery?.capacity ?: "0").toDouble().toString()
            } ?: run {
                "0"
            }
        }
        set(value) {
            currentDevice.value?.let {
                val map = config.deviceBatteryOverrides.toMutableMap()
                map[it.deviceSN] = value
                config.deviceBatteryOverrides = map
            }

            devices = devices.map { it }
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override val batteryCapacityW: Int
        get() = batteryCapacity.toDouble().toInt()

    override var isDemoUser: Boolean
        get() = config.isDemoUser
        set(value) {
            config.isDemoUser = value
            if (value) {
                val demoTheme = AppSettings.demo()
                useColouredFlowLines = demoTheme.useColouredFlowLines
                showBatteryTemperature = demoTheme.showBatteryTemperature
                showBatteryEstimate = demoTheme.showBatteryEstimate
                showSunnyBackground = demoTheme.showSunnyBackground
                decimalPlaces = demoTheme.decimalPlaces
                showFinancialSummary = demoTheme.showFinancialSummary
                showInverterIcon = demoTheme.showInverterIcon
                shouldCombineCT2WithPVPower = demoTheme.shouldCombineCT2WithPVPower
                showFinancialSummaryOnFlowPage = demoTheme.showFinancialSummaryOnFlowPage
                separateParameterGraphsByUnit = demoTheme.separateParameterGraphsByUnit
                powerFlowStrings = demoTheme.powerFlowStrings
                ct2DisplayMode = demoTheme.ct2DisplayMode
            }
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var useColouredFlowLines: Boolean
        get() = config.useColouredFlowLines
        set(value) {
            config.useColouredFlowLines = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var refreshFrequency: RefreshFrequency
        get() = config.refreshFrequency
        set(value) {
            config.refreshFrequency = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showBatteryTemperature: Boolean
        get() = config.showBatteryTemperature
        set(value) {
            config.showBatteryTemperature = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showInverterTemperatures: Boolean
        get() = config.showInverterTemperatures
        set(value) {
            config.showInverterTemperatures = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var useLargeDisplay: Boolean
        get() = config.useLargeDisplay
        set(value) {
            config.useLargeDisplay = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override fun logout(clearDisplaySettings: Boolean, clearDeviceSettings: Boolean) {
        if (clearDisplaySettings) {
            config.clearDisplaySettings()
        }

        if (clearDeviceSettings) {
            config.clearDeviceSettings()
        }

        appSettingsStore.update(AppSettings.toAppSettings(config))
    }

    override var showUsableBatteryOnly: Boolean
        get() = config.showUsableBatteryOnly
        set(value) {
            config.showUsableBatteryOnly = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showInverterIcon: Boolean
        get() = config.showInverterIcon
        set(value) {
            config.showInverterIcon = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showHomeTotal: Boolean
        get() = config.showHomeTotal
        set(value) {
            config.showHomeTotal = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var shouldInvertCT2: Boolean
        get() = config.shouldInvertCT2
        set(value) {
            config.shouldInvertCT2 = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showGridTotals: Boolean
        get() = config.showGridTotals
        set(value) {
            config.showGridTotals = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showInverterTypeNameOnPowerflow: Boolean
        get() = config.showInverterTypeNameOnPowerflow
        set(value) {
            config.showInverterTypeNameOnPowerflow = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showInverterStationNameOnPowerflow: Boolean
        get() = config.showInverterStationNameOnPowerflow
        set(value) {
            config.showInverterStationNameOnPowerflow = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showLastUpdateTimestamp: Boolean
        get() = config.showLastUpdateTimestamp
        set(value) {
            config.showLastUpdateTimestamp = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var solarRangeDefinitions: SolarRangeDefinitions
        get() = config.solarRangeDefinitions
        set(value) {
            config.solarRangeDefinitions = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var totalYieldModel: TotalYieldModel
        get() = config.totalYieldModel
        set(value) {
            config.totalYieldModel = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    final override var devices: List<Device>
        get() {
            return config.devices
        }
        set(value) {
            config.devices = value
            currentDevice.value = devices.firstOrNull { it.deviceSN == selectedDeviceSN }
            selectedDeviceSN = currentDevice.value?.deviceSN
        }

    final override var currentDevice: MutableStateFlow<Device?> = MutableStateFlow(null)

    override var selectedDeviceSN: String?
        get() = config.selectedDeviceSN
        set(value) {
            config.selectedDeviceSN = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override fun select(device: Device) {
        selectedDeviceSN = device.deviceSN
        currentDevice.value = devices.firstOrNull { it.deviceSN == selectedDeviceSN }
    }

    override suspend fun fetchPowerStationDetail() {
        config.powerStationDetail = networking.fetchPowerStationDetail()
    }

    override val variables: List<Variable>
        get() {
            return config.variables
        }

    override suspend fun fetchDevices() {
        try {
            val deviceList = networking.fetchDeviceList()
            config.variables = networking.fetchVariables().mapNotNull { variable ->
                variable.unit?.let {
                    Variable(
                        name = variable.name,
                        variable = variable.variable,
                        unit = it
                    )
                }
            }

            val mappedDevices = ArrayList<Device>()
            deviceList.asFlow().map { networkDevice ->
                val deviceBattery: Battery? = if (networkDevice.hasBattery) {
                    try {
                        val batteryVariables = networking.fetchRealData(networkDevice.deviceSN, listOf("ResidualEnergy", "SoC", "SoC_1"))
                        val batterySettings = networking.fetchBatterySettings(networkDevice.deviceSN)

                        BatteryResponseMapper.map(batteryVariables, batterySettings)
                    } catch (_: Exception) {
                        devices.firstOrNull { it.deviceSN == networkDevice.deviceSN }?.let {
                            Battery(it.battery?.capacity, it.battery?.minSOC, true)
                        }
                    }
                } else {
                    null
                }

                mappedDevices.add(
                    Device(
                        deviceSN = networkDevice.deviceSN,
                        stationName = networkDevice.stationName,
                        stationID = networkDevice.stationID,
                        battery = deviceBattery,
                        moduleSN = networkDevice.moduleSN,
                        hasPV = networkDevice.hasPV,
                        hasBattery = networkDevice.hasBattery,
                        deviceType = networkDevice.deviceType
                    )
                )
            }.collect()

            devices = mappedDevices

            if (selectedDeviceSN == null || !mappedDevices.any { it.deviceSN == selectedDeviceSN }) {
                selectedDeviceSN = devices.firstOrNull()?.deviceSN
                currentDevice.value = devices.firstOrNull { it.deviceSN == selectedDeviceSN }
            }
        } catch (ex: NoSuchElementException) {
            throw NoDeviceFoundException()
        } catch (ex: InvalidTokenException) {
            throw ex
        } catch (ex: SocketTimeoutException) {
            throw ex
        } catch (ex: Exception) {
            throw ex
        }
    }

    override var selectedParameterGraphVariables: List<String>
        get() = config.selectedParameterGraphVariables
        set(value) {
            config.selectedParameterGraphVariables = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var parameterGroups: List<ParameterGroup>
        get() = config.parameterGroups
        set(value) {
            config.parameterGroups = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var solcastSettings: SolcastSettings
        get() = config.solcastSettings
        set(value) {
            config.solcastSettings = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var dataCeiling: DataCeiling
        get() = config.dataCeiling
        set(value) {
            config.dataCeiling = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showFinancialSummaryOnFlowPage: Boolean
        get() = config.showFinancialSummaryOnFlowPage
        set(value) {
            config.showFinancialSummaryOnFlowPage = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var separateParameterGraphsByUnit: Boolean
        get() = config.separateParameterGraphsByUnit
        set(value) {
            config.separateParameterGraphsByUnit = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showBatteryAsPercentage: Boolean
        get() = config.showBatterySOCAsPercentage
        set(value) {
            config.showBatterySOCAsPercentage = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var powerFlowStrings: PowerFlowStringsSettings
        get() = config.powerFlowStrings
        set(value) {
            config.powerFlowStrings = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var powerStationDetail: PowerStationDetail?
        get() = config.powerStationDetail
        set(value) {
            config.powerStationDetail = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showSelfSufficiencyStatsGraphOverlay: Boolean
        get() = config.showSelfSufficiencyStatsGraphOverlay
        set(value) {
            config.showSelfSufficiencyStatsGraphOverlay = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var scheduleTemplates: List<ScheduleTemplate>
        get() = config.scheduleTemplates
        set(value) {
            config.scheduleTemplates = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var truncatedYAxisOnParameterGraphs: Boolean
        get() = config.truncatedYAxisOnParameterGraphs
        set(value) {
            config.truncatedYAxisOnParameterGraphs = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var earningsModel: EarningsModel
        get() = config.earningsModel
        set(value) {
            config.earningsModel = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var summaryDateRange: SummaryDateRange
        get() = config.summaryDateRange
        set(value) {
            config.summaryDateRange = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var lastSolcastRefresh: LocalDateTime?
        get() = config.lastSolcastRefresh
        set(value) {
            config.lastSolcastRefresh = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var widgetTapAction: WidgetTapAction
        get() = config.widgetTapAction
        set(value) {
            config.widgetTapAction = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var batteryTemperatureDisplayMode: BatteryTemperatureDisplayMode
        get() = config.batteryTemperatureDisplayMode
        set(value) {
            config.batteryTemperatureDisplayMode = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showInverterScheduleQuickLink: Boolean
        get() = config.showInverterScheduleQuickLink
        set(value) {
            config.showInverterScheduleQuickLink = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var fetchSolcastOnAppLaunch: Boolean
        get() = config.fetchSolcastOnAppLaunch
        set(value) {
            config.fetchSolcastOnAppLaunch = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var ct2DisplayMode: CT2DisplayMode
        get() = config.ct2DisplayMode
        set(value) {
            config.ct2DisplayMode = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override fun getDeviceSupports(capability: DeviceCapability, deviceSN: String): Boolean {
        return when (capability) {
            DeviceCapability.ScheduleMaxSOC ->
                deviceSupportsScheduleMaxSOC[deviceSN] ?: false

            DeviceCapability.PeakShaving ->
                deviceSupportsPeakShaving[deviceSN] ?: false
        }
    }

    override fun setDeviceSupports(capability: DeviceCapability, deviceSN: String) {
        when (capability) {
            DeviceCapability.ScheduleMaxSOC ->
                deviceSupportsScheduleMaxSOC[deviceSN] = true

            DeviceCapability.PeakShaving ->
                deviceSupportsPeakShaving[deviceSN] = true
        }
    }

    override fun resetDisplaySettings() {
        config.clearDisplaySettings()
        appSettingsStore.update(AppSettings.toAppSettings(config))
        lastSettingsResetTime = LocalDateTime.now()
    }

    override var showStringTotalsAsPercentage: Boolean
        get() = config.showStringTotalsAsPercentage
        set(value) {
            config.showStringTotalsAsPercentage = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var allowNegativeLoad: Boolean
        get() = config.allowNegativeLoad
        set(value) {
            config.allowNegativeLoad = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showInverterConsumption: Boolean
        get() = config.showInverterConsumption
        set(value) {
            config.showInverterConsumption = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showBatterySOCOnDailyStats: Boolean
        get() = config.showBatterySOCOnDailyStats
        set(value) {
            config.showBatterySOCOnDailyStats = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var workModes: List<String>
        get() = config.workModes
        set(value) {
            config.workModes = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override var showOutputEnergyOnStats: Boolean
        get() = config.showOutputEnergyOnStats
        set(value) {
            config.showOutputEnergyOnStats = value
            appSettingsStore.update(AppSettings.toAppSettings(config))
        }

    override fun loginAsDemo() {
        appSettingsStore.update(AppSettings.demo())
    }

    init {
        val selectedSn = selectedDeviceSN
        val initialDevice = if (selectedSn.isNullOrBlank()) {
            devices.firstOrNull()
        } else {
            // Guard against any bad data where deviceSN might unexpectedly be null at runtime
            devices.firstOrNull { it.deviceSN != null && it.deviceSN == selectedSn }
                ?: devices.firstOrNull()
        }

        currentDevice = MutableStateFlow(initialDevice)

        // Keep stored selection consistent if it was missing/invalid
        if (selectedSn.isNullOrBlank() && initialDevice != null) {
            selectedDeviceSN = initialDevice.deviceSN
        }
    }
}

class NoDeviceFoundException : Exception("No device found")
