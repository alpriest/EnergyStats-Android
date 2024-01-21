package com.alpriest.energystats.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

data class OpenApiVariableArray(
    val array: List<OpenApiVariable>
)

data class OpenApiVariable(
    val name: String,
    val variable: String,
    val unit: String?
)

class OpenApiVariableDeserializer : JsonDeserializer<OpenApiVariableArray> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OpenApiVariableArray {
        val resultArray = json.asJsonArray

        val openApiVariables = resultArray.mapNotNull { resultElement ->
            resultElement.asJsonObject.entrySet().firstOrNull()?.let { entry ->
                val variableName = entry.key
                val details = entry.value.asJsonObject
                val unit = details.get("unit")?.asString
                val nameTranslations = details.getAsJsonObject("name")
                val name = nameTranslations.entrySet().firstOrNull { it.key == "en"} ?.value?.asString ?: ""

                OpenApiVariable(name = name, variable = variableName, unit = unit)
            }
        }

        return OpenApiVariableArray(array = openApiVariables)
    }
}