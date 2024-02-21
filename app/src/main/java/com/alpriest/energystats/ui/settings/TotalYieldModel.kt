package com.alpriest.energystats.ui.settings

import android.content.Context
import com.alpriest.energystats.R

enum class TotalYieldModel(val value: Int) {
    Off(0), EnergyStats(1);

    fun title(context: Context): String {
        return when (this) {
            Off -> context.getString(R.string.off)
            EnergyStats -> context.getString(R.string.on)
        }
    }

    companion object {
        fun fromInt(value: Int) = TotalYieldModel.values().firstOrNull { it.value == value } ?: EnergyStats
    }
}