package com.alpriest.energystats.shared.models

enum class InverterGeneration(val value: Int) {
    Unknown(0),
    Generation1(1),
    Generation2(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: Unknown
    }
}