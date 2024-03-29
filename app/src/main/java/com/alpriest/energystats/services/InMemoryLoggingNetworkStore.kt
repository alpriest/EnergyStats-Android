package com.alpriest.energystats.services

import com.alpriest.energystats.models.AddressBookResponse
import com.alpriest.energystats.models.BatteryResponse
import com.alpriest.energystats.models.BatterySOCResponse
import com.alpriest.energystats.models.BatteryTimesResponse
import com.alpriest.energystats.models.DataLoggerResponse
import com.alpriest.energystats.models.OpenApiVariableArray
import com.alpriest.energystats.models.OpenQueryResponse
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.PagedDeviceListResponse
import com.alpriest.energystats.ui.settings.inverter.schedule.EditScheduleStore
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Request
import okhttp3.Response

class InMemoryLoggingNetworkStore {
    val dataLoggerListResponse: MutableStateFlow<NetworkOperation<NetworkResponse<List<DataLoggerResponse>>>?> = MutableStateFlow(null)
    val batteryResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<BatteryResponse>>?> = MutableStateFlow(null)
    val variablesResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<OpenApiVariableArray>>?> = MutableStateFlow(null)
    val rawResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<OpenQueryResponse>>?> = MutableStateFlow(null)
    val addressBookResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<AddressBookResponse>>?> = MutableStateFlow(null)
    val reportResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<OpenReportResponse>>?> = MutableStateFlow(null)
    val batterySOCResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<BatterySOCResponse>>?> = MutableStateFlow(null)
    val deviceListResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<PagedDeviceListResponse>>?> = MutableStateFlow(null)
    val batteryTimesResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<BatteryTimesResponse>>?> = MutableStateFlow(null)
    var latestRequest: String? = null
    var latestResponse: String? = null
    var latestResponseText: String? = null

    companion object {
        val shared: InMemoryLoggingNetworkStore = InMemoryLoggingNetworkStore()
    }
}