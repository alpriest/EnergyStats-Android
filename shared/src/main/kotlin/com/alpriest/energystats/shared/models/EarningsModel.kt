package com.alpriest.energystats.shared.models

enum class EarningsModel(val value: Int) {
    Exported(0),
    Generated(1),
    CT2(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: Exported
    }
}