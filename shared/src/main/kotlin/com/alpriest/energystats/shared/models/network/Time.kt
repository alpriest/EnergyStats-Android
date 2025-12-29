package com.alpriest.energystats.shared.models.network

import com.alpriest.energystats.shared.models.TimeType
import java.time.LocalDateTime

data class Time(
    val hour: Int,
    val minute: Int
) : Comparable<Time> {
    constructor(totalMinutes: Int): this(totalMinutes / 60, totalMinutes % 60)

    companion object {
        fun zero(): Time {
            return Time(0, 0)
        }

        fun now(): Time {
            val now = LocalDateTime.now()
            return Time(now.hour, now.minute)
        }
    }

    fun formatted(timeType: TimeType): String {
        return "${"%02d".format(hour)}:${"%02d".format(minute)}:${timeType.appendage()}"
    }

    override fun compareTo(other: Time): Int {
        return toMinutes().compareTo(other.toMinutes())
    }

    fun toMinutes(): Int {
        return hour * 60 + minute
    }

    fun adding(minutes: Int): Time {
        val newMinutes = toMinutes() + minutes
        return Time(newMinutes)
    }
}