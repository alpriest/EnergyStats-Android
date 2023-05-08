package com.alpriest.energystats.ui.login

import android.util.Log
import com.alpriest.energystats.models.*
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.ui.theme.AppTheme
import com.google.gson.Gson
import kotlinx.coroutines.flow.*

open class ConfigManager(var config: ConfigInterface, val networking: Networking, val rawDataStore: RawDataStoring, override var appVersion: String) : ConfigManaging {
    override val themeStream: MutableStateFlow<AppTheme> = MutableStateFlow(
        AppTheme(
            useLargeDisplay = config.useLargeDisplay,
            useColouredLines = config.useColouredFlowLines,
            showBatteryTemperature = config.showBatteryTemperature,
            decimalPlaces = config.decimalPlaces,
            showSunnyBackground = config.showSunnyBackground,
            showBatteryEstimate = config.showBatteryEstimate,
            showUsableBatteryOnly = config.showUsableBatteryOnly
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

    override var showBatteryEstimate: Boolean
        get() = config.showBatteryEstimate
        set(value) {
            config.showBatteryEstimate = value
            themeStream.value = themeStream.value.copy(showBatteryEstimate = showBatteryEstimate)
        }

    override val minSOC: Double
        get() = (currentDevice.value?.battery?.minSOC ?: "0.2").toDouble()

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

    override var currentDevice: MutableStateFlow<Device?> = MutableStateFlow(
        devices?.firstOrNull { it.deviceID == selectedDeviceID }
    )

    override var selectedDeviceID: String?
        get() = config.selectedDeviceID
        set(value) { config.selectedDeviceID = value }

    override fun select(device: Device) {
        selectedDeviceID = device.deviceID
        Log.d("AWP", "Selected ${device.deviceID}")
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

        try {
            val mappedDevices = ArrayList<Device>()
            deviceList.devices.asFlow().map {
                val batteryCapacity: String?
                val minSOC: String?

                val variables = networking.fetchVariables(it.deviceID)
                val firmware = fetchFirmwareVersions(it.deviceID)

                if (it.hasBattery) {
                    val battery = networking.fetchBattery(it.deviceID)
                    rawDataStore.store(battery = battery)
                    val batterySettings = networking.fetchBatterySettings(it.deviceSN)
                    batteryCapacity = (battery.residual / (battery.soc.toDouble() / 100.0)).toString()
                    minSOC = (batterySettings.minSoc.toDouble() / 100.0).toString()
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
                        variables = variables
                    )
                )
            }.collect()

            devices = mappedDevices
            selectedDeviceID = devices?.firstOrNull()?.deviceID
            rawDataStore.store(deviceList = deviceList)
        } catch (ex: NoSuchElementException) {
            throw NoDeviceFoundException()
        }
    }

    suspend fun fetchFirmwareVersions(deviceID: String): DeviceFirmwareVersion {
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
}

class NoDeviceFoundException : Exception("No device found")
