package com.alpriest.energystats.models

data class PagedDeviceListResponse(
    val currentPage: Int,
    val pageSize: Int,
    val total: Int,
    val devices: List<Device>
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