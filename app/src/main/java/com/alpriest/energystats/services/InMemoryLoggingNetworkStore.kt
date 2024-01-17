package com.alpriest.energystats.services

import com.alpriest.energystats.models.AddressBookResponse
import com.alpriest.energystats.models.BatteryResponse
import com.alpriest.energystats.models.BatterySettingsResponse
import com.alpriest.energystats.models.BatteryTimesResponse
import com.alpriest.energystats.models.DeviceSettingsGetResponse
import com.alpriest.energystats.models.EarningsResponse
import com.alpriest.energystats.models.OpenApiVariableArray
import com.alpriest.energystats.models.PagedDataLoggerListResponse
import com.alpriest.energystats.models.PagedDeviceListResponse
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryLoggingNetworkStore {
    val dataLoggerListResponse: MutableStateFlow<NetworkOperation<NetworkResponse<PagedDataLoggerListResponse>>?> = MutableStateFlow(null)
    val deviceSettingsGetResponse: MutableStateFlow<NetworkOperation<NetworkResponse<DeviceSettingsGetResponse>>?> = MutableStateFlow(null)
    val earningsResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<EarningsResponse>>?> = MutableStateFlow(null)
    val batteryResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<BatteryResponse>>?> = MutableStateFlow(null)
    val variablesResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<OpenApiVariableArray>>?> = MutableStateFlow(null)
    val rawResponseStream: MutableStateFlow<NetworkOperation<NetworkRawResponse>?> = MutableStateFlow(null)
    val addressBookResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<AddressBookResponse>>?> = MutableStateFlow(null)
    val reportResponseStream: MutableStateFlow<NetworkOperation<NetworkReportResponse>?> = MutableStateFlow(null)
    val batterySettingsResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<BatterySettingsResponse>>?> = MutableStateFlow(null)
    val deviceListResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<PagedDeviceListResponse>>?> = MutableStateFlow(null)
    val batteryTimesResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<BatteryTimesResponse>>?> = MutableStateFlow(null)
}