package com.alpriest.energystats.models

data class PagedDeviceListResponse(
    val currentPage: Int,
    val pageSize: Int,
    val total: Int,
    val data: List<PagedDeviceListResponseDevice>
)

class PagedDeviceListResponseDevice(
    val deviceSN: String,
    val moduleSN: String,
    val stationID: String,
    val productType: String,
    val deviceType: String,
    val hasBattery: Boolean,
    val hasPV: Boolean,
    val status: Int
)

class DeviceDetailResponse(
    val deviceSN: String,
    val moduleSN: String,
    val stationID: String,
    val stationName: String,
    val managerVersion: String,
    val masterVersion: String,
    val slaveVersion: String,
    val hardwareVersion: String,
    val status: Int,
    val function: DeviceFunction,
    val productType: String,
    val deviceType: String,
    val hasBattery: Boolean,
    val hasPV: Boolean
)

data class DeviceFunction(
    val scheduler: Boolean
)

data class DeviceListRequest(
    val pageSize: Int = 20,
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
    val deviceSN: String,
    val hasPV: Boolean,
    val stationName: String,
    val stationID: String,
    val hasBattery: Boolean,
    val deviceType: String,
    val battery: Battery?,
    val firmware: DeviceFirmwareVersion?,
    val moduleSN: String
)

data class Battery(
    val capacity: String?,
    val minSOC: String?,
    val hasError: Boolean
)
