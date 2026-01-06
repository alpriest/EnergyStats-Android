package com.alpriest.energystats.ui.login

import com.alpriest.energystats.models.DeviceCapability
import com.alpriest.energystats.shared.network.Networking
import com.alpriest.energystats.services.WidgetTapAction
import com.alpriest.energystats.shared.models.Battery
import com.alpriest.energystats.shared.models.Device
import com.alpriest.energystats.shared.models.ParameterGroup
import com.alpriest.energystats.shared.models.PowerFlowStringsSettings
import com.alpriest.energystats.shared.models.PowerStationDetail
import com.alpriest.energystats.shared.models.ScheduleTemplate
import com.alpriest.energystats.shared.models.SolarRangeDefinitions
import com.alpriest.energystats.shared.models.SolcastSettings
import com.alpriest.energystats.shared.models.StoredConfig
import com.alpriest.energystats.shared.models.SummaryDateRange
import com.alpriest.energystats.shared.models.Variable
import com.alpriest.energystats.shared.network.InvalidTokenException
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.flow.roundedToString
import com.alpriest.energystats.ui.settings.BatteryTemperatureDisplayMode
import com.alpriest.energystats.shared.models.ColorThemeMode
import com.alpriest.energystats.shared.models.DataCeiling
import com.alpriest.energystats.shared.models.DisplayUnit
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.shared.models.SelfSufficiencyEstimateMode
import com.alpriest.energystats.shared.models.TotalYieldModel
import com.alpriest.energystats.ui.settings.financial.EarningsModel
import com.alpriest.energystats.shared.models.CT2DisplayMode
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.demo
import com.alpriest.energystats.ui.theme.toAppTheme
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import java.net.SocketTimeoutException
import java.time.LocalDateTime
import java.util.Locale

open class ConfigManager(var config: StoredConfig, val networking: Networking, override var appVersion: String, override val themeStream: MutableStateFlow<AppTheme>) :
    ConfigManaging {
    private var deviceSupportsScheduleMaxSOC: MutableMap<String, Boolean> = mutableMapOf() // In-memory only
    private var deviceSupportsPeakShaving: MutableMap<String, Boolean> = mutableMapOf() // In-memory only
    override var lastSettingsResetTime: LocalDateTime? = null

    override var showBatteryTimeEstimateOnWidget: Boolean
        get() = config.showBatteryTimeEstimateOnWidget
        set(value) {
            config.showBatteryTimeEstimateOnWidget = value
        }

    override var useTraditionalLoadFormula: Boolean
        get() = config.useTraditionalLoadFormula
        set(value) {
            config.useTraditionalLoadFormula = value
        }

    override var colorThemeMode: ColorThemeMode
        get() = ColorThemeMode.fromInt(config.colorTheme)
        set(value) {
            config.colorTheme = value.value
            themeStream.value = themeStream.value.copy(colorTheme = colorThemeMode)
        }

    override var showGraphValueDescriptions: Boolean
        get() = config.showGraphValueDescriptions
        set(value) {
            config.showGraphValueDescriptions = value
            themeStream.value = themeStream.value.copy(showGraphValueDescriptions = showGraphValueDescriptions)
        }

    override var shouldCombineCT2WithPVPower: Boolean
        get() = config.shouldCombineCT2WithPVPower
        set(value) {
            config.shouldCombineCT2WithPVPower = value
            themeStream.value = themeStream.value.copy(shouldCombineCT2WithPVPower = shouldCombineCT2WithPVPower)
        }

    override var shouldCombineCT2WithLoadsPower: Boolean
        get() = config.shouldCombineCT2WithLoadsPower
        set(value) {
            config.shouldCombineCT2WithLoadsPower = value
            themeStream.value = themeStream.value.copy(shouldCombineCT2WithLoadsPower = shouldCombineCT2WithLoadsPower)
        }

    override var currencyCode: String
        get() = config.currencyCode
        set(value) {
            config.currencyCode = value
        }

    override var currencySymbol: String
        get() = config.currencySymbol
        set(value) {
            config.currencySymbol = value
            themeStream.value = themeStream.value.copy(currencySymbol = currencySymbol)
        }

    override var gridImportUnitPrice: Double
        get() = config.gridImportUnitPrice
        set(value) {
            config.gridImportUnitPrice = value
        }

    override var feedInUnitPrice: Double
        get() = config.feedInUnitPrice
        set(value) {
            config.feedInUnitPrice = value
        }

    override var decimalPlaces: Int
        get() = config.decimalPlaces
        set(value) {
            config.decimalPlaces = value
            themeStream.value = themeStream.value.copy(decimalPlaces = decimalPlaces)
        }

    override var showSunnyBackground: Boolean
        get() = config.showSunnyBackground
        set(value) {
            config.showSunnyBackground = value
            themeStream.value = themeStream.value.copy(showSunnyBackground = showSunnyBackground)
        }

    override var selfSufficiencyEstimateMode: SelfSufficiencyEstimateMode
        get() = SelfSufficiencyEstimateMode.fromInt(config.selfSufficiencyEstimateMode)
        set(value) {
            config.selfSufficiencyEstimateMode = value.value
            themeStream.value = themeStream.value.copy(selfSufficiencyEstimateMode = selfSufficiencyEstimateMode)
        }

    override var showFinancialSummary: Boolean
        get() = config.showFinancialSummary
        set(value) {
            config.showFinancialSummary = value
            themeStream.value = themeStream.value.copy(showFinancialSummary = showFinancialSummary)
        }

    override var showBatteryEstimate: Boolean
        get() = config.showBatteryEstimate
        set(value) {
            config.showBatteryEstimate = value
            themeStream.value = themeStream.value.copy(showBatteryEstimate = showBatteryEstimate)
        }

    override var displayUnit: DisplayUnit
        get() = DisplayUnit.fromInt(config.displayUnit)
        set(value) {
            config.displayUnit = value.value
            themeStream.value = themeStream.value.copy(displayUnit = displayUnit)
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
                    devices = devices?.map {
                        if (it.deviceSN == updatedDevice.deviceSN) updatedDevice else it
                    }
                }
            }
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

            devices = devices?.map { it }
        }

    override val batteryCapacityW: Int
        get() = batteryCapacity.toDouble().toInt()

    override var isDemoUser: Boolean
        get() = config.isDemoUser
        set(value) {
            config.isDemoUser = value
            if (value) {
                val demoTheme = AppTheme.demo()
                useColouredFlowLines = demoTheme.useColouredLines
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
        }

    override var useColouredFlowLines: Boolean
        get() = config.useColouredFlowLines
        set(value) {
            config.useColouredFlowLines = value
            themeStream.value = themeStream.value.copy(useColouredLines = useColouredFlowLines)
        }

    override var refreshFrequency: RefreshFrequency
        get() = RefreshFrequency.fromInt(config.refreshFrequency)
        set(value) {
            config.refreshFrequency = value.value
        }

    override var showBatteryTemperature: Boolean
        get() = config.showBatteryTemperature
        set(value) {
            config.showBatteryTemperature = value
            themeStream.value = themeStream.value.copy(showBatteryTemperature = showBatteryTemperature)
        }

    override var showInverterTemperatures: Boolean
        get() = config.showInverterTemperatures
        set(value) {
            config.showInverterTemperatures = value
            themeStream.value = themeStream.value.copy(showInverterTemperatures = showInverterTemperatures)
        }

    override var useLargeDisplay: Boolean
        get() = config.useLargeDisplay
        set(value) {
            config.useLargeDisplay = value
            themeStream.value = themeStream.value.copy(useLargeDisplay = useLargeDisplay)
        }

    override fun logout(clearDisplaySettings: Boolean, clearDeviceSettings: Boolean) {
        if (clearDisplaySettings) {
            config.clearDisplaySettings()
        }

        if (clearDeviceSettings) {
            config.clearDeviceSettings()
        }
    }

    override var showUsableBatteryOnly: Boolean
        get() = config.showUsableBatteryOnly
        set(value) {
            config.showUsableBatteryOnly = value
            themeStream.value = themeStream.value.copy(showUsableBatteryOnly = showUsableBatteryOnly)
        }

    override var showInverterIcon: Boolean
        get() = config.showInverterIcon
        set(value) {
            config.showInverterIcon = value
            themeStream.value = themeStream.value.copy(showInverterIcon = showInverterIcon)
        }

    override var showHomeTotal: Boolean
        get() = config.showHomeTotal
        set(value) {
            config.showHomeTotal = value
            themeStream.value = themeStream.value.copy(showHomeTotal = showHomeTotal)
        }

    override var shouldInvertCT2: Boolean
        get() = config.shouldInvertCT2
        set(value) {
            config.shouldInvertCT2 = value
            themeStream.value = themeStream.value.copy(shouldInvertCT2 = shouldInvertCT2)
        }

    override var showGridTotals: Boolean
        get() = config.showGridTotals
        set(value) {
            config.showGridTotals = value
            themeStream.value = themeStream.value.copy(showGridTotals = showGridTotals)
        }

    override var showInverterTypeNameOnPowerflow: Boolean
        get() = config.showInverterTypeNameOnPowerflow
        set(value) {
            config.showInverterTypeNameOnPowerflow = value
            themeStream.value = themeStream.value.copy(showInverterTypeNameOnPowerflow = showInverterTypeNameOnPowerflow)
        }

    override var showInverterStationNameOnPowerflow: Boolean
        get() = config.showInverterStationNameOnPowerflow
        set(value) {
            config.showInverterStationNameOnPowerflow = value
            themeStream.value = themeStream.value.copy(showInverterStationNameOnPowerflow = showInverterStationNameOnPowerflow)
        }

    override var showLastUpdateTimestamp: Boolean
        get() = config.showLastUpdateTimestamp
        set(value) {
            config.showLastUpdateTimestamp = value
            themeStream.value = themeStream.value.copy(showLastUpdateTimestamp = showLastUpdateTimestamp)
        }

    override var solarRangeDefinitions: SolarRangeDefinitions
        get() = config.solarRangeDefinitions
        set(value) {
            config.solarRangeDefinitions = value
            themeStream.value = themeStream.value.copy(solarRangeDefinitions = solarRangeDefinitions)
        }

    override var totalYieldModel: TotalYieldModel
        get() = TotalYieldModel.fromInt(config.totalYieldModel)
        set(value) {
            config.totalYieldModel = value.value
            themeStream.value = themeStream.value.copy(totalYieldModel = totalYieldModel)
        }

    final override var devices: List<Device>?
        get() {
            config.devices?.let {
                return Gson().fromJson(it, Array<Device>::class.java).toList()
            }

            return null
        }
        set(value) {
            if (value != null) {
                config.devices = Gson().toJson(value)
            } else {
                config.devices = null
            }

            currentDevice.value = devices?.firstOrNull { it.deviceSN == selectedDeviceSN }
        }

    final override var currentDevice: MutableStateFlow<Device?> = MutableStateFlow(null)

    override var selectedDeviceSN: String?
        get() = config.selectedDeviceSN
        set(value) {
            config.selectedDeviceSN = value
        }

    override fun select(device: Device) {
        selectedDeviceSN = device.deviceSN
        currentDevice.value = devices?.firstOrNull { it.deviceSN == selectedDeviceSN }
    }

    override suspend fun fetchPowerStationDetail() {
        config.powerStationDetail = networking.fetchPowerStationDetail()
    }

    override val variables: List<Variable>
        get() {
            return config.variables
        }

    override suspend fun fetchDevices() {
        var method = "openapi_fetchDeviceList"
        try {
            val deviceList = networking.fetchDeviceList()
            method = "openapi_fetchVariables"
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
                        devices?.firstOrNull { it.deviceSN == networkDevice.deviceSN }?.let {
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
                selectedDeviceSN = devices?.firstOrNull()?.deviceSN
                currentDevice.value = devices?.firstOrNull { it.deviceSN == selectedDeviceSN }
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
        }

    override var parameterGroups: List<ParameterGroup>
        get() = config.parameterGroups
        set(value) {
            config.parameterGroups = value
            themeStream.value = themeStream.value.copy(parameterGroups = parameterGroups)
        }

    override var solcastSettings: SolcastSettings
        get() = config.solcastSettings
        set(value) {
            config.solcastSettings = value
            themeStream.value = themeStream.value.copy(solcastSettings = solcastSettings)
        }

    override var dataCeiling: DataCeiling
        get() = DataCeiling.fromInt(config.dataCeiling)
        set(value) {
            config.dataCeiling = value.value
            themeStream.value = themeStream.value.copy(dataCeiling = dataCeiling)
        }

    override var showFinancialSummaryOnFlowPage: Boolean
        get() = config.showFinancialSummaryOnFlowPage
        set(value) {
            config.showFinancialSummaryOnFlowPage = value
            themeStream.value = themeStream.value.copy(showFinancialSummaryOnFlowPage = showFinancialSummaryOnFlowPage)
        }

    override var separateParameterGraphsByUnit: Boolean
        get() = config.separateParameterGraphsByUnit
        set(value) {
            config.separateParameterGraphsByUnit = value
            themeStream.value = themeStream.value.copy(separateParameterGraphsByUnit = separateParameterGraphsByUnit)
        }

    override var showBatteryAsPercentage: Boolean
        get() = config.showBatterySOCAsPercentage
        set(value) {
            config.showBatterySOCAsPercentage = value
            themeStream.value = themeStream.value.copy(showBatterySOCAsPercentage = showBatteryAsPercentage)
        }

    override var powerFlowStrings: PowerFlowStringsSettings
        get() = config.powerFlowStrings
        set(value) {
            config.powerFlowStrings = value
            themeStream.value = themeStream.value.copy(powerFlowStrings = powerFlowStrings)
        }

    override var powerStationDetail: PowerStationDetail?
        get() = config.powerStationDetail
        set(value) {
            config.powerStationDetail = value
        }

    override var showSelfSufficiencyStatsGraphOverlay: Boolean
        get() = config.showSelfSufficiencyStatsGraphOverlay
        set(value) {
            config.showSelfSufficiencyStatsGraphOverlay = value
        }

    override var scheduleTemplates: List<ScheduleTemplate>
        get() = config.scheduleTemplates
        set(value) {
            config.scheduleTemplates = value
        }

    override var truncatedYAxisOnParameterGraphs: Boolean
        get() = config.truncatedYAxisOnParameterGraphs
        set(value) {
            config.truncatedYAxisOnParameterGraphs = value
            themeStream.value = themeStream.value.copy(truncatedYAxisOnParameterGraphs = truncatedYAxisOnParameterGraphs)
        }

    override var earningsModel: EarningsModel
        get() = EarningsModel.fromInt(config.earningsModel)
        set(value) {
            config.earningsModel = value.value
        }

    override var summaryDateRange: SummaryDateRange
        get() = config.summaryDateRange
        set(value) {
            config.summaryDateRange = value
        }

    override var lastSolcastRefresh: LocalDateTime?
        get() = config.lastSolcastRefresh
        set(value) {
            config.lastSolcastRefresh = value
        }

    override var widgetTapAction: WidgetTapAction
        get() = WidgetTapAction.fromInt(config.widgetTapAction)
        set(value) {
            config.widgetTapAction = value.value
        }

    override var batteryTemperatureDisplayMode: BatteryTemperatureDisplayMode
        get() = BatteryTemperatureDisplayMode.fromInt(config.batteryTemperatureDisplayMode)
        set(value) {
            config.batteryTemperatureDisplayMode = value.value
        }

    override var showInverterScheduleQuickLink: Boolean
        get() = config.showInverterScheduleQuickLink
        set(value) {
            config.showInverterScheduleQuickLink = value
            themeStream.value = themeStream.value.copy(showInverterScheduleQuickLink = showInverterScheduleQuickLink)
        }

    override var fetchSolcastOnAppLaunch: Boolean
        get() = config.fetchSolcastOnAppLaunch
        set(value) {
            config.fetchSolcastOnAppLaunch = value
        }

    override var ct2DisplayMode: CT2DisplayMode
        get() = CT2DisplayMode.fromInt(config.ct2DisplayMode)
        set(value) {
            config.ct2DisplayMode = value.value
            themeStream.value = themeStream.value.copy(ct2DisplayMode = ct2DisplayMode)
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
        themeStream.value = AppTheme.toAppTheme(config)
        lastSettingsResetTime = LocalDateTime.now()
    }

    override var showStringTotalsAsPercentage: Boolean
        get() = config.showStringTotalsAsPercentage
        set(value) {
            config.showStringTotalsAsPercentage = value
            themeStream.value = themeStream.value.copy(showStringTotalsAsPercentage = showStringTotalsAsPercentage)
        }

    override var allowNegativeHouseLoad: Boolean
        get() = config.allowNegativeHouseLoad
        set(value) {
            config.allowNegativeHouseLoad = value
            themeStream.value = themeStream.value.copy(allowNegativeHouseLoad = allowNegativeHouseLoad)
        }

    override var showInverterConsumption: Boolean
        get() = config.showInverterConsumption
        set(value) {
            config.showInverterConsumption = value
            themeStream.value = themeStream.value.copy(showInverterConsumption = showInverterConsumption)
        }

    override var showBatterySOCOnDailyStats: Boolean
        get() = config.showBatterySOCOnDailyStats
        set(value) {
            config.showBatterySOCOnDailyStats = value
            themeStream.value = themeStream.value.copy(showBatterySOCOnDailyStats = showBatterySOCOnDailyStats)
        }

    override var workModes: List<String>
        get() = config.workModes
        set(value) {
            config.workModes = value
        }

    init {
        currentDevice = MutableStateFlow(devices?.firstOrNull { it.deviceSN == selectedDeviceSN })
    }
}

class DataFetchFailure(method: String, ex: Exception) : Exception("Could not fetch $method (#${ex.localizedMessage})")
class NoDeviceFoundException : Exception("No device found")
