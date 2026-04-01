package com.alpriest.energystats.shared.models

import android.content.Context
import com.alpriest.energystats.shared.R

enum class InverterGeneration(val value: Int) {
    Unknown(0),
    Generation1(1),
    Generation2(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: Unknown
    }

    fun title(context: Context): String {
        return when (this) {
            Unknown -> context.getString(R.string.unknown)
            Generation1 -> context.getString(R.string.generation_1)
            Generation2 -> context.getString(R.string.generation_2)
        }
    }
}