package com.alpriest.energystats.models

import java.time.LocalDate

data class RawRequest(
    val deviceID: String,
    val variables: Array<String>,
    val timespan: String = "day",
    val beginDate: QueryDate
) {
    constructor(deviceID: String, variables: Array<RawVariable>) : this(
        deviceID = deviceID,
        variables = variables.map { it.networkTitle() }.toTypedArray(),
        beginDate = QueryDate()
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

data class QueryDate(val year: Int, val month: Int, val day: Int) {
    companion object {
        operator fun invoke(): QueryDate {
            val date = LocalDate.now()
            return QueryDate(year = date.year, month = date.monthValue, day = date.dayOfMonth)
        }
    }
}

data class RawResponse(
    val variable: String,
    val data: ArrayList<RawData>
)

data class RawData(
    val time: String,
    val value: Double
)
