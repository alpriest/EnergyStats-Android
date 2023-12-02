package com.alpriest.energystats.ui.settings

import android.content.Context
import com.alpriest.energystats.R

enum class DisplayUnit(val value: Int) {
    Kilowatts(0),
    Watts(1),
    Adaptive(2);

    fun title(context: Context): String {
        return when (this) {
            Kilowatts -> context.getString(R.string.kilowatts)
            Watts -> context.getString(R.string.watts)
            Adaptive -> context.getString(R.string.adaptive)
        }
    }

    companion object {
        fun fromInt(value: Int) = DisplayUnit.values().first { it.value == value }
    }
}

enum class DataCeiling(val value: Int) {
    None(0),
    Mild(1),
    Enhanced(2);

    fun title(context: Context): String {
        return when (this) {
            None -> context.getString(R.string.none)
            Mild -> context.getString(R.string.mild)
            Enhanced -> context.getString(R.string.enhanced)
        }
    }

    companion object {
        fun fromInt(value: Int) = DataCeiling.values().first { it.value == value }
    }
}