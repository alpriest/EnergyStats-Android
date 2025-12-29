package com.alpriest.energystats.shared.models

enum class TimeType {
    START,
    END;

    fun appendage(): String {
        return when (this) {
            START -> "00"
            END -> "59"
        }
    }
}