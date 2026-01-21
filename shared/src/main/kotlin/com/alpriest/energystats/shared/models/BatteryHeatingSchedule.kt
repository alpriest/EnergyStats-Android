package com.alpriest.energystats.shared.models

import com.alpriest.energystats.shared.models.network.BatteryHeatingScheduleResponse
import com.alpriest.energystats.shared.models.network.Time

data class BatteryHeatingSchedule(
    val enabled: Boolean,
    val warmUpState: String?,
    val period1Start: Time,
    val period1End: Time,
    val period1Enabled: Boolean,
    val period2Start: Time,
    val period2End: Time,
    val period2Enabled: Boolean,
    val period3Start: Time,
    val period3End: Time,
    val period3Enabled: Boolean,
    val startTemperature: Double,
    val endTemperature: Double,
    val minStartTemperature: Double,
    val maxStartTemperature: Double,
    val minEndTemperature: Double,
    val maxEndTemperature: Double
) {
    companion object {
        fun make(response: BatteryHeatingScheduleResponse): BatteryHeatingSchedule {
            val dict = response.dataList.associate { it.name to it.value }
            val enabled = boolean("batteryWarmUpEnable", dict)
            val minStartTemperature = double("minStartTemperatureRange", dict)
            val maxStartTemperature = double("maxStartTemperatureRange", dict)
            val minEndTemperature = double("minEndTemperatureRange", dict)
            val maxEndTemperature = double("maxEndTemperatureRange", dict)
            val period1Enabled = boolean("time1Enable", dict)
            val period2Enabled = boolean("time2Enable", dict)
            val period3Enabled = boolean("time3Enable", dict)
            val warmUpState = dict["batteryWarmUpState"]
            val startTemperature = double("startTemperature", dict)
            val endTemperature = double("endTemperature", dict)
            val period1 = time(
                startHourKey = "time1StartHour",
                startMinuteKey = "time1StartMinute",
                endHourKey = "time1EndHour",
                endMinuteKey = "time1EndMinute",
                dict
            )
            val period2 = time(
                startHourKey = "time2StartHour",
                startMinuteKey = "time2StartMinute",
                endHourKey = "time2EndHour",
                endMinuteKey = "time2EndMinute",
                dict
            )
            val period3 = time(
                startHourKey = "time3StartHour",
                startMinuteKey = "time3StartMinute",
                endHourKey = "time3EndHour",
                endMinuteKey = "time3EndMinute",
                dict
            )

            return BatteryHeatingSchedule(
                enabled = enabled,
                warmUpState = warmUpState,
                period1Start = period1.first,
                period1End = period1.second,
                period1Enabled = period1Enabled,
                period2Start = period2.first,
                period2End = period2.second,
                period2Enabled = period2Enabled,
                period3Start = period3.first,
                period3End = period3.second,
                period3Enabled = period3Enabled,
                startTemperature = startTemperature,
                endTemperature = endTemperature,
                minStartTemperature = minStartTemperature,
                maxStartTemperature = maxStartTemperature,
                minEndTemperature = minEndTemperature,
                maxEndTemperature = maxEndTemperature
            )
        }

        private fun time(startHourKey: String, startMinuteKey: String, endHourKey: String, endMinuteKey: String, dictionary: Map<String, String>): Pair<Time, Time> {
            val startHour = int(startHourKey, dictionary)
            val startMinute = int(startMinuteKey, dictionary)
            val endHour = int(endHourKey, dictionary)
            val endMinute = int(endMinuteKey, dictionary)

            return Pair(Time(startHour, startMinute), Time(endHour, endMinute))
        }

        private fun int(name: String, dictionary: Map<String, String>): Int {
            val raw = required(name, dictionary)
            return raw.toInt()
        }

        private fun double(name: String, dictionary: Map<String, String>): Double {
            val raw = required(name, dictionary)
            return raw.toDouble()
        }

        private fun boolean(name: String, dictionary: Map<String, String>): Boolean {
            val raw = required(name, dictionary)
            return when (raw.lowercase()) {
                "true", "1", "on", "yes", "enable" -> true
                "false", "0", "off", "no", "disable" -> false
                else -> throw IllegalArgumentException()
            }
        }

        private fun required(name: String, dictionary: Map<String, String>): String {
            return dictionary.getValue(name)
        }
    }
}