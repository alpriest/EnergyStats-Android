package com.alpriest.energystats.models

import com.alpriest.energystats.ui.statsgraph.ReportType

data class ReportResponse(
    val variable: String,
    val data: Array<ReportData>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReportResponse

        if (variable != other.variable) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = variable.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

data class ReportRequest(
    val deviceID: String,
    val variables: Array<String>,
    val queryDate: QueryDate,
    val reportType: ReportType,
) {
    constructor(deviceID: String, variables: Array<ReportVariable>, queryDate: QueryDate, reportType: ReportType) : this(
        deviceID = deviceID,
        variables = variables.map { it.networkTitle() }.toTypedArray(),
        queryDate = queryDate,
        reportType = reportType
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReportRequest

        if (deviceID != other.deviceID) return false
        if (reportType != other.reportType) return false
        if (!variables.contentEquals(other.variables)) return false
        if (queryDate != other.queryDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deviceID.hashCode()
        result = 31 * result + reportType.hashCode()
        result = 31 * result + variables.contentHashCode()
        result = 31 * result + queryDate.hashCode()
        return result
    }
}

data class ReportData(
    val index: Int,
    val value: Double
)
