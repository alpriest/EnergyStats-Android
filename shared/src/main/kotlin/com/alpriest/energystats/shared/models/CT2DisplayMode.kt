package com.alpriest.energystats.shared.models

import android.content.Context
import com.alpriest.energystats.shared.R

enum class CT2DisplayMode(val value: Int) {
    Hidden(0),
    SeparateIcon(1),
    AsPowerString(2);

    fun title(context: Context): String {
        return when (this) {
            Hidden -> context.getString(R.string.ct2displaymode_hidden)
            SeparateIcon -> context.getString(R.string.ct2displaymode_separate_icon)
            AsPowerString -> context.getString(R.string.ct2displaymode_as_power_string)
        }
    }

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: Hidden
    }
}