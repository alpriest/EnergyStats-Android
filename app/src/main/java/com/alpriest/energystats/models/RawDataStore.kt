package com.alpriest.energystats.models

import kotlinx.coroutines.flow.MutableStateFlow

@Deprecated("Use InMemoryLoggingNetworkStore")
class RawDataStore : RawDataStoring {
    override val rawStream: MutableStateFlow<List<RawResponse>?> = MutableStateFlow(null)
    override var batterySettingsStream: MutableStateFlow<BatterySettingsResponse?> = MutableStateFlow(null)
    override var batteryStream: MutableStateFlow<BatteryResponse?> = MutableStateFlow(null)
    override var deviceListStream: MutableStateFlow<PagedDeviceListResponse?> = MutableStateFlow(null)

    override fun store(raw: List<RawResponse>) = run { rawStream.value = raw }
    override fun store(batterySettings: BatterySettingsResponse) = run { batterySettingsStream.value = batterySettings }
    override fun store(battery: BatteryResponse) = run { batteryStream.value = battery }
    override fun store(deviceList: PagedDeviceListResponse) = run { deviceListStream.value = deviceList }
}