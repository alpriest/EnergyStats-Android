package com.alpriest.energystats.ui.settings

enum class DisplayUnit(val value: Int) {
    Kilowatts(0),
    Watts(1),
    Adaptive(2);

    fun title(): String {
        return when (this) {
            Kilowatts -> "Kilowatts"
            Watts -> "Watts"
            Adaptive -> "Adaptive"
        }
    }

    companion object {
        fun fromInt(value: Int) = DisplayUnit.values().first { it.value == value }
    }
}