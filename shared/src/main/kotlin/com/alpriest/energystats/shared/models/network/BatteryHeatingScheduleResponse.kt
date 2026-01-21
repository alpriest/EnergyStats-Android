package com.alpriest.energystats.shared.models.network

data class GetBatteryHeatingScheduleRequest(
    val sn: String
)

data class BatteryHeatingScheduleResponse(
    val dataList: List<BatteryHeatingParameter>
)

data class BatteryHeatingParameter(
    val name: String,
    val value: String,
    val unit: String?
)

data class BatteryHeatingScheduleRequest(
    val sn: String,
    val batteryWarmUpEnable: String,
    val startTemperature: String,
    val endTemperature: String,
    val time1Enable: String,
    val time1StartHour: String,
    val time1StartMinute: String,
    val time1EndHour: String,
    val time1EndMinute: String,
    val time2Enable: String,
    val time2StartHour: String,
    val time2StartMinute: String,
    val time2EndHour: String,
    val time2EndMinute: String,
    val time3Enable: String,
    val time3StartHour: String,
    val time3StartMinute: String,
    val time3EndHour: String,
    val time3EndMinute : String
)