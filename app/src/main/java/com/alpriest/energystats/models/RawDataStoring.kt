package com.alpriest.energystats.models

import kotlinx.coroutines.flow.MutableStateFlow

interface RawDataStoring {
    val rawStream: MutableStateFlow<List<RawResponse>?>
    fun store(raw: List<RawResponse>)

    var batterySettingsStream: MutableStateFlow<BatterySOCResponse?>
    fun store(batterySettings: BatterySOCResponse)

    var batteryStream: MutableStateFlow<BatteryResponse?>
    fun store(battery: BatteryResponse)

    var deviceListStream: MutableStateFlow<PagedDeviceListResponse?>
    fun store(deviceList: PagedDeviceListResponse)
}
