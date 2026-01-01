package com.alpriest.energystats.shared.models

object SharedDataKeys {
    const val TOKEN = "token"
    const val DEVICE_SN = "deviceSN"

    // Power flow values
    const val SOLAR_AMOUNT = "solarAmount"
    const val HOUSE_LOAD_AMOUNT = "houseLoadAmount"
    const val BATTERY_AMOUNT = "batteryAmount"
    const val GRID_AMOUNT = "gridAmount"

    // Config / settings
    const val SHOW_GRID_TOTALS = "showGridTotalsOnPowerFlow"
    const val BATTERY_CAPACITY = "batteryCapacity"
    const val SHOULD_INVERT_CT2 = "shouldInvertCT2"
    const val MIN_SOC = "minSOC"
    const val SHOULD_COMBINE_CT2_WITH_PV = "shouldCombineCT2WithPVPower"
    const val SHOW_USABLE_BATTERY_ONLY = "showUsableBatteryOnly"

    // Nested maps
    const val SOLAR_RANGE_DEFINITIONS = "solarRangeDefinitions"
    const val THRESHOLD_1 = "threshold1"
    const val THRESHOLD_2 = "threshold2"
    const val THRESHOLD_3 = "threshold3"

    // Meta
    const val UPDATED_AT = "updatedAt"
}