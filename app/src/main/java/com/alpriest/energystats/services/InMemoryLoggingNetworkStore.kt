package com.alpriest.energystats.services

import com.alpriest.energystats.models.BatterySOCResponse
import com.alpriest.energystats.models.BatteryTimesResponse
import com.alpriest.energystats.models.DataLoggerResponse
import com.alpriest.energystats.models.OpenApiVariableArray
import com.alpriest.energystats.models.OpenRealQueryResponse
import com.alpriest.energystats.models.OpenReportResponse
import com.alpriest.energystats.models.PagedDeviceListResponse
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryLoggingNetworkStore {
    val reportResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<List<OpenReportResponse>>>?> = MutableStateFlow(null)
    val realQueryResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<List<OpenRealQueryResponse>>>?> = MutableStateFlow(null)
    val deviceListResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<PagedDeviceListResponse>>?> = MutableStateFlow(null)
    val variablesResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<OpenApiVariableArray>>?> = MutableStateFlow(null)
    val batterySOCResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<BatterySOCResponse>>?> = MutableStateFlow(null)
    val batteryTimesResponseStream: MutableStateFlow<NetworkOperation<NetworkResponse<BatteryTimesResponse>>?> = MutableStateFlow(null)
    val dataLoggerListResponse: MutableStateFlow<NetworkOperation<NetworkResponse<List<DataLoggerResponse>>>?> = MutableStateFlow(null)

    var latestRequest: String? = null
    var latestResponse: String? = null
    var latestResponseText: String? = null

    companion object {
        val shared: InMemoryLoggingNetworkStore = InMemoryLoggingNetworkStore()
    }
}