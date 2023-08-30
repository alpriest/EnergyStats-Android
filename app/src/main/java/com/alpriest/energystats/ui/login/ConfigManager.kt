package com.alpriest.energystats.ui.login

import com.alpriest.energystats.models.*
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.ui.settings.SelfSufficiencyEstimateMode
import com.alpriest.energystats.ui.theme.AppTheme
import com.google.gson.Gson
import kotlinx.coroutines.flow.*

open class ConfigManager(var config: ConfigInterface, val networking: Networking, override var appVersion: String) : ConfigManaging {
    override val themeStream: MutableStateFlow<AppTheme> = MutableStateFlow(
        AppTheme(
            useLargeDisplay = config.useLargeDisplay,
            useColouredLines = config.useColouredFlowLines,
            showBatteryTemperature = config.showBatteryTemperature,
            decimalPlaces = config.decimalPlaces,
            showSunnyBackground = config.showSunnyBackground,
            showBatteryEstimate = config.showBatteryEstimate,
            showUsableBatteryOnly = config.showUsableBatteryOnly,
            showTotalYield = config.showTotalYield,
            selfSufficiencyEstimateMode = SelfSufficiencyEstimateMode.fromInt(config.selfSufficiencyEstimateMode),
            showEstimatedEarnings = config.showEstimatedEarnings,
            showValuesInWatts = config.showValuesInWatts,
            showInverterTemperatures = config.showInverterTemperatures,
            showInverterIcon = config.showInverterIcon,
            showHomeTotal = config.showHomeTotal,
            shouldInvertCT2 = config.shouldInvertCT2
        )
    )

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

    override var showTotalYield: Boolean
        get() = config.showTotalYield
        set(value) {
            config.showTotalYield = value
            themeStream.value = themeStream.value.copy(showTotalYield = showTotalYield)
        }

    override var showEstimatedEarnings: Boolean
        get() = config.showEstimatedEarnings
        set(value) {
            config.showEstimatedEarnings = value
            themeStream.value = themeStream.value.copy(showEstimatedEarnings = showEstimatedEarnings)
        }

    override var showBatteryEstimate: Boolean
        get() = config.showBatteryEstimate
        set(value) {
            config.showBatteryEstimate = value
            themeStream.value = themeStream.value.copy(showBatteryEstimate = showBatteryEstimate)
        }

    override var showValuesInWatts: Boolean
        get() = config.showValuesInWatts
        set(value) {
            config.showValuesInWatts = value
            themeStream.value = themeStream.value.copy(showValuesInWatts = showValuesInWatts)
        }

    override val minSOC: MutableStateFlow<Double?> = MutableStateFlow(null)

    override val batteryCapacity: Int
        get() = (currentDevice.value?.battery?.capacity ?: "2600").toDouble().toInt()

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
        config.devices = null
        config.isDemoUser = false
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

            currentDevice.value = devices?.firstOrNull { it.deviceID == selectedDeviceID }
        }

    final override var currentDevice: MutableStateFlow<Device?> = MutableStateFlow(null)

    override var selectedDeviceID: String?
        get() = config.selectedDeviceID
        set(value) {
            config.selectedDeviceID = value
        }

    override fun select(device: Device) {
        selectedDeviceID = device.deviceID
        currentDevice.value = devices?.firstOrNull { it.deviceID == selectedDeviceID }
    }

    override val variables: List<RawVariable>
        get() {
            return currentDevice.value?.variables ?: listOf()
        }

    override val hasBattery: Boolean
        get() {
            return currentDevice.value?.let { it.battery == null } ?: false
        }

    override suspend fun fetchDevices() {
        val deviceList = networking.fetchDeviceList()
        var currentAction = ""

        try {
            val mappedDevices = ArrayList<Device>()
            deviceList.devices.asFlow().map {
                var batteryCapacity: String?
                var minSOC: String?

                currentAction = "fetch variables"
                val variables = networking.fetchVariables(it.deviceID)
                currentAction = "fetch firmware versions"
                val firmware = fetchFirmwareVersions(it.deviceID)

                if (it.hasBattery) {
                    currentAction = "fetch battery"
                    val battery = networking.fetchBattery(it.deviceID)
                    currentAction = "fetch battery settings"
                    val batterySettings = networking.fetchBatterySettings(it.deviceSN)
                    try {
                        batteryCapacity = (battery.residual / (battery.soc.toDouble() / 100.0)).toString()
                        minSOC = (batterySettings.minGridSoc.toDouble() / 100.0).toString()
                    } catch (_: Exception) {
                        batteryCapacity = null
                        minSOC = null
                    }
                } else {
                    batteryCapacity = null
                    minSOC = null
                }

                mappedDevices.add(
                    Device(
                        plantName = it.plantName,
                        deviceID = it.deviceID,
                        deviceSN = it.deviceSN,
                        hasPV = it.hasPV,
                        battery = if (it.hasBattery) Battery(batteryCapacity, minSOC) else null,
                        deviceType = it.deviceType,
                        firmware = firmware,
                        variables = variables,
                        moduleSN = it.moduleSN
                    )
                )
            }.collect()

            devices = mappedDevices
            selectedDeviceID = devices?.firstOrNull()?.deviceID
        } catch (ex: NoSuchElementException) {
            throw NoDeviceFoundException()
        } catch (ex: Exception) {
            throw CouldNotFetchDeviceList(currentAction, ex)
        }
    }

    override suspend fun refreshFirmwareVersion() {
        try {
            devices = devices?.map {
                val firmware = fetchFirmwareVersions(it.deviceID)
                if (it.firmware != firmware) {
                    return@map it.copy(firmware = firmware)
                } else {
                    return@map it
                }
            }
        } catch (ex: Exception) {
            // Ignore
        }
    }

    private suspend fun fetchFirmwareVersions(deviceID: String): DeviceFirmwareVersion {
        val firmware = networking.fetchAddressBook(deviceID)

        return DeviceFirmwareVersion(
            master = firmware.softVersion.master,
            slave = firmware.softVersion.slave,
            manager = firmware.softVersion.manager
        )
    }

    override fun updateBatteryCapacity(capacity: String) {
        devices = devices?.map {
            if (it.deviceID == selectedDeviceID && it.battery != null) {
                it.copy(battery = Battery(capacity, it.battery.minSOC))
            } else {
                it
            }
        }
    }

    override var selectedParameterGraphVariables: List<String>
        get() = config.selectedParameterGraphVariables
        set(value) {
            config.selectedParameterGraphVariables = value
        }

    init {
        currentDevice.onEach {
            minSOC.value = it?.battery?.minSOC?.toDouble()
        }
        currentDevice = MutableStateFlow(devices?.firstOrNull { it.deviceID == selectedDeviceID })
    }
}

class CouldNotFetchDeviceList(message: String, ex: Exception) : Exception("Could not fetch device list (${message}) (#${ex.localizedMessage})")
class NoDeviceFoundException : Exception("No device found")
