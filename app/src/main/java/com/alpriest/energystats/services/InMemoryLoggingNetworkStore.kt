package com.alpriest.energystats.services

import com.alpriest.energystats.models.AddressBookResponse
import com.alpriest.energystats.models.BatteryResponse
import com.alpriest.energystats.models.BatterySettingsResponse
import com.alpriest.energystats.models.BatteryTimesResponse
import com.alpriest.energystats.models.DeviceSettingsGetResponse
import com.alpriest.energystats.models.EarningsResponse
import com.alpriest.energystats.models.PagedDeviceListResponse
import com.alpriest.energystats.models.RawResponse
import com.alpriest.energystats.models.VariablesResponse
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryLoggingNetworkStore {
    val deviceSettingsGetResponse: MutableStateFlow<NetworkOperation<NetworkResponse<DeviceSettingsGetResponse>>?> = MutableStateFlow(null)
    val earningsResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<EarningsResponse>>?> = MutableStateFlow(null)
    var batteryResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<BatteryResponse>>?> = MutableStateFlow(null)
    var variablesResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<VariablesResponse>>?> = MutableStateFlow(null)
    var rawResponseStream: MutableStateFlow<NetworkOperation<NetworkRawResponse>?> = MutableStateFlow(null)
    var addressBookResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<AddressBookResponse>>?> = MutableStateFlow(null)
    var reportResponseStream: MutableStateFlow<NetworkOperation<NetworkReportResponse>?> = MutableStateFlow(null)
    var batterySettingsResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<BatterySettingsResponse>>?> = MutableStateFlow(null)
    var deviceListResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<PagedDeviceListResponse>>?> = MutableStateFlow(null)
    val batteryTimesResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<BatteryTimesResponse>>?> = MutableStateFlow(null)
}