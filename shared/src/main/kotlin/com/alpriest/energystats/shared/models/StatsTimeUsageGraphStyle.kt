package com.alpriest.energystats.shared.models

import android.content.Context
import com.alpriest.energystats.shared.R

enum class StatsTimeUsageGraphStyle(val value: Int) {
    Bar(0),
    Line(1),
    Off(2);

    fun title(context: Context): String {
        return when (this) {
            Bar -> context.getString(R.string.bar)
            Line -> context.getString(R.string.line)
            Off -> context.getString(R.string.hidden)
        }
    }

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: Bar
    }
}