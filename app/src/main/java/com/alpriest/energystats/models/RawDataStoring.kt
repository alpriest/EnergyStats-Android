package com.alpriest.energystats.models

interface RawDataStoring {
    var raw: List<RawResponse>?
    var batterySettings: BatterySettingsResponse?
    var battery: BatteryResponse?
    var deviceList: PagedDeviceListResponse?
}
