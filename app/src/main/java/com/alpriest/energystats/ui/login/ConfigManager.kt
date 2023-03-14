package com.alpriest.energystats.ui.login

import com.alpriest.energystats.models.Battery
import com.alpriest.energystats.models.ConfigInterface
import com.alpriest.energystats.models.Device
import com.alpriest.energystats.models.RawDataStoring
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging
import com.alpriest.energystats.ui.settings.RefreshFrequency
import com.alpriest.energystats.ui.theme.AppTheme
import com.google.gson.Gson
import kotlinx.coroutines.flow.*

class ConfigManager(var config: ConfigInterface, val networking: Networking, val rawDataStore: RawDataStoring) : ConfigManaging {
    override val themeStream: MutableStateFlow<AppTheme> = MutableStateFlow(
        AppTheme(
            useLargeDisplay = config.useLargeDisplay,
            useColouredLines = config.useColouredFlowLines,
            showBatteryTemperature = config.showBatteryTemperature,
            decimalPlaces = config.decimalPlaces,
            showSunnyBackground = config.showSunnyBackground,
            showBatteryEstimate = config.showBatteryEstimate
        )
    )

    override var decimalPlaces: Int
        get() = config.decimalPlaces
        set(value) {
            config.decimalPlaces = value
            themeStream.value = themeStream.value.update(decimalPlaces = decimalPlaces)
        }

    override var showSunnyBackground: Boolean
        get() = config.showSunnyBackground
        set(value) {
            config.showSunnyBackground = value
            themeStream.value = themeStream.value.update(showSunnyBackground = showSunnyBackground)
        }

    override var showBatteryEstimate: Boolean
        get() = config.showBatteryEstimate
        set(value) {
            config.showBatteryEstimate = value
            themeStream.value = themeStream.value.update(showBatteryEstimate = showBatteryEstimate)
        }

    override val minSOC: Double
        get() = (currentDevice?.battery?.minSOC ?: "0.2").toDouble()

    override val batteryCapacity: Int
        get() = (currentDevice?.battery?.capacity ?: "2600").toDouble().toInt()

    override var isDemoUser: Boolean
        get() = config.isDemoUser
        set(value) {
            config.isDemoUser = value
        }

    override var useColouredFlowLines: Boolean
        get() = config.useColouredFlowLines
        set(value) {
            config.useColouredFlowLines = value
            themeStream.value = themeStream.value.update(useColouredLines = useColouredFlowLines)
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
            themeStream.value = themeStream.value.update(showBatteryTemperature = showBatteryTemperature)
        }

    override var useLargeDisplay: Boolean
        get() = config.useLargeDisplay
        set(value) {
            config.useLargeDisplay = value
            themeStream.value = themeStream.value.update(useLargeDisplay = useLargeDisplay)
        }

    override fun logout() {
        config.devices = null
        config.isDemoUser = false
    }

    override var devices: List<Device>?
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
        }

    override var currentDevice: Device?
        get() {
            return devices?.first { it.deviceID == selectedDeviceID }
        }
        set(@Suppress("UNUSED_PARAMETER") value) {}

    override var selectedDeviceID: String?
        get() = config.selectedDeviceID
        set(value) {
            config.selectedDeviceID = value
        }

    override suspend fun findDevices() {
        val deviceList = networking.fetchDeviceList()

        try {
            val mappedDevices = ArrayList<Device>()
            deviceList.devices.asFlow().map {
                val batteryCapacity: String?
                val minSOC: String?

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
                        battery = if (it.hasBattery) Battery(batteryCapacity, minSOC) else null
                    )
                )
            }.collect()

            devices = mappedDevices
            selectedDeviceID = devices?.first()?.deviceID
            rawDataStore.store(deviceList = deviceList)
        } catch (ex: NoSuchElementException) {
            throw NoDeviceFoundException()
        }
    }

    override fun updateBatteryCapacity(capacity: String) {
        devices = devices?.map {
            if (it.deviceID == selectedDeviceID && it.battery != null) {
                Device(it.plantName, it.deviceID, it.deviceSN, it.hasPV, Battery(capacity, it.battery.minSOC))
            } else {
                it
            }
        }
    }
}

class NoDeviceFoundException : Exception("No device found")
