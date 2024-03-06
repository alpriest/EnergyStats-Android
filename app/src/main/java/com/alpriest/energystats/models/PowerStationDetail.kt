package com.alpriest.energystats.models

data class PowerStationDetail(val stationName: String, val capacity: Double, val timezone: String) {
    companion object {
        val defaults: PowerStationDetail
            get() {
                return PowerStationDetail("", 0.0, "")
            }
    }
}