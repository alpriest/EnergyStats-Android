package com.alpriest.energystats.services

import android.content.Context
import com.alpriest.energystats.R

enum class WidgetTapAction(val value: Int) {
    Launch(0),
    Refresh(1);

    fun title(context: Context): String {
        return when (this) {
            Launch -> context.getString(R.string.launch)
            Refresh -> context.getString(R.string.refresh)
        }
    }

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: Launch
    }
}