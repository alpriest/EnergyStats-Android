package com.alpriest.energystats.models

import java.util.Calendar
import java.util.Date
import java.util.TimeZone

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

data class QueryDate(val year: Int, val month: Int?, val day: Int?) {
    companion object {
        operator fun invoke(): QueryDate {
            val calendar = Calendar.getInstance()
            return QueryDate(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH) + 1,
                day = calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}

fun QueryDate.toUtcMillis(): Long {
    val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
        set(Calendar.YEAR, year)
        // Adjust month to 0-based index used by Calendar
        set(Calendar.MONTH, (month ?: 1) - 1)
        set(Calendar.DAY_OF_MONTH, day ?: 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

fun QueryDate.toDate(): Date {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, (month ?: 1) - 1) // Month is 0-based in Calendar
    calendar.set(Calendar.DAY_OF_MONTH, day ?: 1) // Default to 1st if null
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
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
