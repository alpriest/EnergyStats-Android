package com.alpriest.energystats.models

data class PagedDeviceListResponse(
    val currentPage: Int,
    val pageSize: Int,
    val total: Int,
    val devices: List<NetworkDevice>
)

class NetworkDevice(
    val plantName: String,
    val deviceID: String,
    val deviceSN: String,
    val hasBattery: Boolean,
    val hasPV: Boolean
)

data class DeviceListRequest(
    val pageSize: Int = 1,
    val currentPage: Int = 1,
    val total: Int = 0,
    val condition: Condition = Condition()
)

data class Condition(
    val queryDate: DeviceListQueryDate = DeviceListQueryDate()
)

data class DeviceListQueryDate(
    val begin: Int = 0,
    val end: Int = 0
)

data class DeviceList(
    val devices: Array<DeviceList>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceList

        if (!devices.contentEquals(other.devices)) return false

        return true
    }

    override fun hashCode(): Int {
        return devices.contentHashCode()
    }
}

data class Device(
    val plantName: String,
    val deviceID: String,
    val deviceSN: String,
    val hasPV: Boolean,
    val battery: Battery?
)

data class Battery(
    val capacity: String?,
    val minSOC: String?
)
