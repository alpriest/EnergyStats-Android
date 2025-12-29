package com.alpriest.energystats.shared.services

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonPrimitive
import com.alpriest.energystats.shared.models.DataLoggerStatus
import com.alpriest.energystats.shared.models.OpenApiVariable
import com.alpriest.energystats.shared.models.OpenApiVariableArray
import com.alpriest.energystats.shared.models.OpenQueryResponseData
import com.alpriest.energystats.shared.models.OpenRealQueryResponse
import com.alpriest.energystats.shared.models.OpenReportResponse
import com.alpriest.energystats.shared.models.OpenReportResponseData
import java.lang.reflect.Type

class OpenApiVariableDeserializer : JsonDeserializer<OpenApiVariableArray> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OpenApiVariableArray {
        val resultArray = json.asJsonArray

        val openApiVariables = resultArray.mapNotNull { resultElement ->
            resultElement.asJsonObject.entrySet().firstOrNull()?.let { entry ->
                val variableName = entry.key
                val details = entry.value.asJsonObject
                val unit = details.get("unit")?.asString
                val nameTranslations = details.getAsJsonObject("name")
                val name = nameTranslations.entrySet().firstOrNull { it.key == "en" }?.value?.asString ?: ""

                OpenApiVariable(name = name, variable = variableName, unit = unit)
            }
        }

        return OpenApiVariableArray(array = openApiVariables)
    }
}

class OpenReportResponseDeserializer : JsonDeserializer<OpenReportResponse> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OpenReportResponse {
        val resultObject = json.asJsonObject
        val resultArray = resultObject.get("values").asJsonArray

        val values = resultArray.mapIndexedNotNull { index, entry ->
            if (entry != JsonNull.INSTANCE) {
                OpenReportResponseData(index + 1, entry.asDouble)
            } else {
                null
            }
        }

        val variable = resultObject.get("variable").asString
        val unit = resultObject.get("unit").asString

        return OpenReportResponse(variable, unit, values)
    }
}

class DataLoggerStatusDeserializer : JsonDeserializer<DataLoggerStatus> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): DataLoggerStatus {
        val value = json.asInt
        return DataLoggerStatus.values().firstOrNull { it.value == value } ?: DataLoggerStatus.UNKNOWN
    }
}

class OpenRealQueryResponseDeserializer : JsonDeserializer<OpenRealQueryResponse> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OpenRealQueryResponse {
        val resultObject = json.asJsonObject
        val resultArray = resultObject.get("datas").asJsonArray

        val values = resultArray.mapNotNull { element ->
            val details = element.asJsonObject
            val variable = details.get("variable").asString
            val unit = details.get("unit")?.asString
            var value: Double? = null
            var valueString: String? = null

            val jsonElement = details.get("value")
            if (jsonElement is JsonPrimitive) {
                when {
                    jsonElement.isNumber -> value = jsonElement.asDouble
                    jsonElement.isString -> valueString = jsonElement.asString
                }
            }

            OpenQueryResponseData(unit, variable, value, valueString)
        }

        val time = resultObject.get("time").asString
        val deviceSN = resultObject.get("deviceSN").asString

        return OpenRealQueryResponse(time, deviceSN, values)
    }
}