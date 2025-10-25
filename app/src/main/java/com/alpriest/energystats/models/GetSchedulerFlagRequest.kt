package com.alpriest.energystats.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

data class GetSchedulerFlagRequest(
    val deviceSN: String
)

data class GetSchedulerFlagResponse(
    val enable: Boolean,
    val support: Boolean
)

data class SetSchedulerFlagRequest(
    val deviceSN: String,
    val enable: Int
)

data class ScheduleResponse(
    val enable: Int,
    val groups: List<SchedulePhaseNetworkModel>,
    val workModes: List<String>
) {
    class Deserializer : JsonDeserializer<ScheduleResponse> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ScheduleResponse {
            val jsonObject = json.asJsonObject

            val enable = jsonObject["enable"].asInt
            val groups = context.deserialize<List<SchedulePhaseNetworkModel>>(
                jsonObject["groups"],
                object : TypeToken<List<SchedulePhaseNetworkModel>>() {}.type
            )

            // Use the registered WorkMode deserializer automatically
            val workModes = context.deserialize<List<String>>(
                jsonObject
                    .getAsJsonObject("properties")
                    .getAsJsonObject("workmode")
                    .getAsJsonArray("enumList"),
                object : TypeToken<List<String>>() {}.type
            )

            return ScheduleResponse(enable, groups, workModes)
        }
    }}

data class SchedulePhaseNetworkModel(
    val enable: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val workMode: String,
    val minSocOnGrid: Int,
    val fdSoc: Int,
    val fdPwr: Int?,
    val maxSoc: Int?
)

data class SetCurrentScheduleRequest(
    val deviceSN: String,
    val groups: List<SchedulePhaseNetworkModel>
)