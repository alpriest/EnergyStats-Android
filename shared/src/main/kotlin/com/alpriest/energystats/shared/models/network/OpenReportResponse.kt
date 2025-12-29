package com.alpriest.energystats.shared.models.network

data class OpenReportResponse(
    val variable: String,
    val unit: String,
    val values: List<OpenReportResponseData>
)

data class OpenReportResponseData(
    val index: Int,
    val value: Double
)

data class OpenReportRequest(
    val sn: String,
    val variables: List<String>,
    val dimension: ReportType,
    val year: Int,
    val month: Int?,
    val day: Int?
)

@Suppress("EnumEntryName")
enum class ReportType {
    day,
    month,
    year,
}
