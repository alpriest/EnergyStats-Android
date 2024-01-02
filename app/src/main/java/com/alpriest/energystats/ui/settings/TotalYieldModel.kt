package com.alpriest.energystats.ui.settings

import android.content.Context
import com.alpriest.energystats.R

enum class TotalYieldModel(val value: Int) {
    Off(0), EnergyStats(0), FoxESS(1);

    fun title(context: Context): String {
        return when (this) {
            Off -> context.getString(R.string.off)
            EnergyStats -> context.getString(R.string.pv_power)
            FoxESS -> context.getString(R.string.foxess)
        }
    }

    companion object {
        fun fromInt(value: Int) = TotalYieldModel.values().first { it.value == value }
    }
}