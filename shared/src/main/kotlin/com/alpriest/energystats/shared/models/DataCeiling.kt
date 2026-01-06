package com.alpriest.energystats.shared.models

import android.content.Context
import com.alpriest.energystats.shared.R

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
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: None
    }
}