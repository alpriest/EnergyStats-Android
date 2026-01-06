package com.alpriest.energystats.shared.models

import android.content.Context
import com.alpriest.energystats.shared.R

enum class SelfSufficiencyEstimateMode(val value: Int) {
    Off(0),
    Net(1),
    Absolute(2);

    fun title(context: Context): String {
        return when (this) {
            Net -> context.getString(R.string.net)
            Absolute -> context.getString(R.string.absolute)
            else -> context.getString(R.string.off)
        }
    }

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: Off
    }
}