package com.alpriest.energystats.ui.settings

import android.content.Context
import com.alpriest.energystats.R

enum class BatteryTemperatureDisplayMode(val value: Int) {
    Automatic(0),
    Battery1(1),
    Battery2(2);

    fun title(context: Context): String {
        return when (this) {
            Automatic -> context.getString(R.string.automatic)
            Battery1 -> context.getString(R.string.battery1)
            Battery2 -> context.getString(R.string.battery2)
        }
    }

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: Automatic
    }
}