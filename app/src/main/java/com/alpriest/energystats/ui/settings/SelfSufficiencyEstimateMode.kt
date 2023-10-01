package com.alpriest.energystats.ui.settings

enum class SelfSufficiencyEstimateMode(val value: Int) {
    Off(0),
    Net(1),
    Absolute(2);

    fun title(): String {
        return when (this) {
            Net -> "Net"
            Absolute -> "Absolute"
            else -> "Off"
        }
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}