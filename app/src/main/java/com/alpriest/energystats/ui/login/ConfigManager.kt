package com.alpriest.energystats.ui.login

import com.alpriest.energystats.models.*
import com.alpriest.energystats.services.FoxESSNetworking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.paramsgraph.editing.ParameterGroup
import com.alpriest.energystats.ui.settings.ColorThemeMode
import com.alpriest.energystats.ui.settings.DataCeiling
import com.alpriest.energystats.ui.settings.DisplayUnit
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.ui.settings.SelfSufficiencyEstimateMode
import com.alpriest.energystats.ui.settings.TotalYieldModel
import com.alpriest.energystats.ui.settings.solcast.SolcastSettings
import com.alpriest.energystats.ui.theme.AppTheme
import com.alpriest.energystats.ui.theme.SolarRangeDefinitions
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

open class ConfigManager(var config: ConfigInterface, val networking: FoxESSNetworking, override var appVersion: String, override val themeStream: MutableStateFlow<AppTheme>) :
    ConfigManaging {

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

    override var currencyCode: String
        get() = config.currencyCode
        set(value) {
            config.currencyCode = value
        }

    override var currencySymbol: String
        get() = config.currencySymbol
        set(value) {
            config.currencySymbol = value
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

    override val minSOC: MutableStateFlow<Double?> = MutableStateFlow(null)

    override var batteryCapacity: Int
        get() {
            return currentDevice.value?.let {
                val override = config.deviceBatteryOverrides[it.deviceSN]
                return (override ?: it.battery?.capacity ?: "0").toDouble().toInt()
            } ?: run {
                10000
            }
        }
        set(value) {
            currentDevice.value?.let {
                val map = config.deviceBatteryOverrides.toMutableMap()
                map[it.deviceSN] = value.toString()
                config.deviceBatteryOverrides = map
            }

            devices = devices?.map { it }
        }

    override var isDemoUser: Boolean
        get() = config.isDemoUser
        set(value) {
            config.isDemoUser = value
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

    override fun logout() {
        config.clear()
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

    override var showInverterPlantNameOnPowerflow: Boolean
        get() = config.showInverterPlantNameOnPowerflow
        set(value) {
            config.showInverterPlantNameOnPowerflow = value
            themeStream.value = themeStream.value.copy(showInverterPlantNameOnPowerflow = showInverterPlantNameOnPowerflow)
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

    override val variables: List<Variable>
        get() {
            return config.variables
        }

    override val hasBattery: Boolean
        get() {
            return currentDevice.value?.let { it.battery == null } ?: false
        }

    override suspend fun fetchDevices() {
        try {
            val deviceList = networking.openapi_fetchDeviceList()
            config.variables = networking.openapi_fetchVariables().mapNotNull { variable ->
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
                        val batteryVariables = networking.openapi_fetchRealData(networkDevice.deviceSN, listOf("ResidualEnergy", "SoC"))
                        val batterySettings = networking.openapi_fetchBatterySOC(networkDevice.deviceSN)

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
                        firmware = DeviceFirmwareVersion(manager = networkDevice.managerVersion, master = networkDevice.masterVersion, slave = networkDevice.slaveVersion),
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
        } catch (ex: Exception) {
            throw DataFetchFailure("Failed to load Device or Battery Settings", ex)
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

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        currentDevice = MutableStateFlow(devices?.firstOrNull { it.deviceSN == selectedDeviceSN })
        coroutineScope.launch {
            currentDevice.collect {
                minSOC.value = it?.battery?.minSOC?.toDouble()
            }
        }
    }
}

class DataFetchFailure(method: String, ex: Exception) : Exception("Could not fetch $method (#${ex.localizedMessage})")
class NoDeviceFoundException : Exception("No device found")
