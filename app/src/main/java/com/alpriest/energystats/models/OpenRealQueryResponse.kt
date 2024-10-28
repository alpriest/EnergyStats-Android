package com.alpriest.energystats.models

import com.alpriest.energystats.ui.statsgraph.ReportType

data class OpenRealQueryRequest(
    val deviceSN: String,
    val variables: List<String>
)

data class OpenRealQueryResponse(
    val time: String,
    val deviceSN: String,
    val datas: List<OpenQueryResponseData>
)

data class OpenQueryResponseData(
    val unit: String?,
    val variable: String,
    val value: Double?,
    val valueString: String?
)

data class OpenHistoryRequest(
    val sn: String,
    val variables: List<String>,
    val begin: Long,
    val end: Long
)

data class OpenHistoryResponse(
    val deviceSN: String,
    val datas: List<OpenHistoryResponseData>
)

data class OpenHistoryResponseData(
    val unit: String?,
    val name: String,
    val variable: String,
    val data: List<UnitData>
)

data class UnitData(
    val time: String,
    val value: Double
)

data class OpenReportResponse(
    val variable: String,
    val unit: String,
    val values: List<OpenReportResponseData>
)

data class OpenReportResponseData(
    val index: Int,
    val value: Double
)

data class BatterySOCResponse(
    val minSocOnGrid: Int,
    val minSoc: Int
) {
    fun minSocOnGridPercent(): Double {
        return minSocOnGrid.toDouble() / 100.0
    }
}

data class SetBatterySOCRequest(
    val minSocOnGrid: Int,
    val minSoc: Int,
    val sn: String
)

data class OpenReportRequest(
    val sn: String,
    val variables: List<String>,
    val dimension: ReportType,
    val year: Int,
    val month: Int?,
    val day: Int?
)