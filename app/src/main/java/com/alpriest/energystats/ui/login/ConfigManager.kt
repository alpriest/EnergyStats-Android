package com.alpriest.energystats.ui.login

import com.alpriest.energystats.models.ConfigInterface
import com.alpriest.energystats.services.Networking
import com.alpriest.energystats.stores.ConfigManaging

class ConfigManager(var config: ConfigInterface, val networking: Networking) : ConfigManaging {
    override val minSOC: Double
        get() = (config.minSOC ?: "0.0").toDouble()

    override val batteryCapacityW: Int
        get() = (config.batteryCapacity ?: "2600").toDouble().toInt()

    override val deviceSN: String?
        get() = config.deviceSN

    override val deviceID: String?
        get() = config.deviceID

    override val hasPV: Boolean
        get() = config.hasPV

    override var hasBattery: Boolean
        get() = config.hasBattery
        set(value) {
            config.hasBattery = value
        }

    override var isDemoUser: Boolean
        get() = config.isDemoUser
        set(value) {
            config.isDemoUser = value
        }

    override fun logout() {
        config.deviceID = null
        config.deviceSN = null
        config.hasPV = false
        config.hasBattery = false
        config.isDemoUser = false
    }

    override suspend fun findDevice() {
        val deviceList = networking.fetchDeviceList()

        try {
            val device = deviceList.devices.first()
            config.deviceSN = device.deviceSN
            config.deviceID = device.deviceID
            config.hasBattery = device.hasBattery
            config.hasPV = device.hasPV
            if (device.hasBattery) {
                val battery = networking.fetchBattery()
                val batterySettings = networking.fetchBatterySettings()
                config.batteryCapacity =
                    (battery.residual / (battery.soc.toDouble() / 100.0)).toString()
                config.minSOC = (batterySettings.minSoc.toDouble() / 100.0).toString()
            }

        } catch (ex: NoSuchElementException) {
            throw NoDeviceFoundException()
        }
    }
}

class NoDeviceFoundException : Exception("No device found") {

}
