package com.alpriest.energystats.shared.models

import android.content.Context

enum class StatsTimeUsageGraphStyle(val value: Int) {
    Bar(0),
    Line(1),
    Off(2);

    fun title(context: Context): String {
        return when (this) {
            StatsTimeUsageGraphStyle.Bar -> "Bar"
            StatsTimeUsageGraphStyle.Line -> "Line"
            StatsTimeUsageGraphStyle.Off -> "Hidden"
        }
    }

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: Bar
    }
}