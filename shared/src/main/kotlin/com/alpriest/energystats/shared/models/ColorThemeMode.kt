package com.alpriest.energystats.shared.models

import android.content.Context
import com.alpriest.energystats.shared.R

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
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: Auto
    }
}