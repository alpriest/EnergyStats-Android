package com.alpriest.energystats.shared.models.network

import com.alpriest.energystats.shared.network.ScheduleResponseSerializer
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.Serializable
import java.lang.reflect.Type
import com.google.gson.JsonElement

@Serializable(with = ScheduleResponseSerializer::class)
data class ScheduleResponse(
    val enable: Int,
    val groups: List<SchedulePhaseResponse>,
    val workModes: List<String>,
    val maxGroupCount: Int,
    val properties: Map<String, SchedulePropertyDefinition>
) {
    class Deserializer : JsonDeserializer<ScheduleResponse> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ScheduleResponse {
            val jsonObject = json.asJsonObject

            val enable = jsonObject["enable"].asInt
            val groups = context.deserialize<List<SchedulePhaseResponse>>(
                jsonObject["groups"],
                object : TypeToken<List<SchedulePhaseResponse>>() {}.type
            )

            val maxGroupCount = jsonObject["maxGroupCount"].asInt
            val properties = context.deserialize<Map<String, SchedulePropertyDefinition>>(
                jsonObject["properties"],
                object : TypeToken<Map<String, SchedulePropertyDefinition>>() {}.type
            )
            val workModes = properties["workmode"]?.enumList ?: emptyList()

            return ScheduleResponse(enable, groups, workModes, maxGroupCount, properties)
        }
    }
}

@Serializable
data class SchedulePropertyDefinition(
    val enumList: List<String>?,
    val precision: Double,
    val range: SchedulePropertyDefinitionRange?,
    val unit: String
)

@Serializable
data class SchedulePropertyDefinitionRange(
    val max: Double,
    val min: Double
)