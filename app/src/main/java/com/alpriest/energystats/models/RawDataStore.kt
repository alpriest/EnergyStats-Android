package com.alpriest.energystats.models

class RawDataStore : RawDataStoring {
    override var raw: List<RawResponse>? = null
    override var batterySettings: BatterySettingsResponse? = null
    override var battery: BatteryResponse? = null
    override var deviceList: PagedDeviceListResponse? = null
}