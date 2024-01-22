package com.alpriest.energystats.ui.settings

import android.content.Context
import com.alpriest.energystats.R

enum class ColorThemeMode(val value: Int) {
    Auto(0),
    Light(1),
    Dark(2);

    fun title(context: Context): String {
        return when (this) {
            Auto -> context.getString(R.string.auto)
            Light -> context.getString(R.string.light)
            Dark -> context.getString(R.string.dark)
        }
    }

    companion object {
        fun fromInt(value: Int) = ColorThemeMode.values().first { it.value == value }
    }
}