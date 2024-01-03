package com.alpriest.energystats.ui.settings

import android.content.Context
import com.alpriest.energystats.R

enum class TotalYieldModel(val value: Int) {
    Off(0), EnergyStats(1), FoxESS(2);

    fun title(context: Context): String {
        return when (this) {
            Off -> context.getString(R.string.off)
            EnergyStats -> context.getString(R.string.pv_only)
            FoxESS -> context.getString(R.string.foxess)
        }
    }

    companion object {
        fun fromInt(value: Int) = TotalYieldModel.values().first { it.value == value }
    }
}