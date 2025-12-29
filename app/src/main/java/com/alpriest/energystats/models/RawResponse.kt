package com.alpriest.energystats.models

import com.alpriest.energystats.shared.models.QueryDate

data class RawRequest(
    val deviceID: String,
    val variables: Array<String>,
    val timespan: String = "day",
    val beginDate: QueryDate
) {
    constructor(deviceID: String, variables: List<Variable>, queryDate: QueryDate) : this(
        deviceID = deviceID,
        variables = variables.map { it.variable }.toTypedArray(),
        beginDate = queryDate
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawRequest

        if (deviceID != other.deviceID) return false
        if (!variables.contentEquals(other.variables)) return false
        if (timespan != other.timespan) return false
        if (beginDate != other.beginDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deviceID.hashCode()
        result = 31 * result + variables.contentHashCode()
        result = 31 * result + timespan.hashCode()
        result = 31 * result + beginDate.hashCode()
        return result
    }
}

data class RawResponse(
    val variable: String,
    val data: Array<RawData>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawResponse

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

data class RawData(
    val time: String,
    val value: Double
)
