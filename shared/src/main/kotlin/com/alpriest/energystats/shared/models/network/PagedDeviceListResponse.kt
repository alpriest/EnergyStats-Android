package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.Serializable

@Serializable
data class PagedDeviceListResponse(
    val currentPage: Int,
    val pageSize: Int,
    val total: Int,
    val data: List<DeviceSummaryResponse>
)

@Serializable
class DeviceSummaryResponse(
    val deviceSN: String,
    val moduleSN: String,
    val stationID: String,
    val stationName: String,
    val productType: String,
    val deviceType: String,
    val hasBattery: Boolean,
    val hasPV: Boolean,
    val status: Int
)

@Serializable
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
    val hasPV: Boolean,
    val batteryList: List<DeviceBatteryResponse>?
)

@Serializable
data class DeviceBatteryResponse(
    val batterySN: String,
    val type: String,
    val version: String
)

@Serializable
data class DeviceFunction(
    val scheduler: Boolean
)

data class DeviceListRequest(
    val pageSize: Int = 20,
    val currentPage: Int = 1
)
