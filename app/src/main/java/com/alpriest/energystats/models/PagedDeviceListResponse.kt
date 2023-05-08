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
    val hasPV: Boolean,
    val deviceType: String
)

data class DeviceListRequest(
    val pageSize: Int = 10,
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

data class Device(
    val plantName: String,
    val deviceID: String,
    val deviceSN: String,
    val hasPV: Boolean,
    val battery: Battery?,
    val deviceType: String?,
    val firmware: DeviceFirmwareVersion?,
    val variables: List<RawVariable>
)

data class Battery(
    val capacity: String?,
    val minSOC: String?
)
