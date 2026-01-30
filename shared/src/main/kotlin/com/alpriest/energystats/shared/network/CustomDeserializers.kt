package com.alpriest.energystats.shared.network

import com.alpriest.energystats.shared.models.ParameterGroup
import com.alpriest.energystats.shared.models.network.ApiVariable
import com.alpriest.energystats.shared.models.network.ApiVariableArray
import com.alpriest.energystats.shared.models.network.DataLoggerStatus
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class OpenApiVariableDeserializer : JsonDeserializer<ApiVariableArray> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ApiVariableArray {
        val resultArray = json.asJsonArray

        val apiVariables = resultArray.mapNotNull { resultElement ->
            resultElement.asJsonObject.entrySet().firstOrNull()?.let { entry ->
                val variableName = entry.key
                val details = entry.value.asJsonObject
                val unit = details.get("unit")?.asString
                val nameTranslations = details.getAsJsonObject("name")
                val name = nameTranslations.entrySet().firstOrNull { it.key == "en" }?.value?.asString ?: ""

                ApiVariable(name = name, variable = variableName, unit = unit)
            }
        }

        return ApiVariableArray(array = apiVariables)
    }
}

class DataLoggerStatusDeserializer : JsonDeserializer<DataLoggerStatus> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): DataLoggerStatus {
        val value = json.asInt
        return DataLoggerStatus.entries.firstOrNull { it.value == value } ?: DataLoggerStatus.UNKNOWN
    }
}

class ParameterGroupDeserializer : JsonDeserializer<ParameterGroup> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ParameterGroup {
        val obj = json.asJsonObject

        val id = obj["id"]?.asString ?: ""
        val title = obj["title"]?.asString ?: ""

        val names: List<String> =
            obj["parameterNames"]
                ?.takeIf { it.isJsonArray }
                ?.asJsonArray
                ?.mapNotNull { it.takeIf(JsonElement::isJsonPrimitive)?.asString }
                ?: emptyList()

        return ParameterGroup(
            id = id,
            title = title,
            parameterNames = names
        )
    }
}