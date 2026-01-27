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

//class OpenReportResponseDeserializer : JsonDeserializer<OpenReportResponse> {
//    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OpenReportResponse {
//        val resultObject = json.asJsonObject
//        val resultArray = resultObject.get("values").asJsonArray
//
//        val values = resultArray.mapIndexedNotNull { index, entry ->
//            if (entry != JsonNull.INSTANCE) {
//                OpenReportResponseData(index + 1, entry.asDouble)
//            } else {
//                null
//            }
//        }
//
//        val variable = resultObject.get("variable").asString
//        val unit = resultObject.get("unit").asString
//
//        return OpenReportResponse(variable, unit, values)
//    }
//}

class DataLoggerStatusDeserializer : JsonDeserializer<DataLoggerStatus> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): DataLoggerStatus {
        val value = json.asInt
        return DataLoggerStatus.values().firstOrNull { it.value == value } ?: DataLoggerStatus.UNKNOWN
    }
}
//class OpenRealQueryResponseDeserializer : JsonDeserializer<OpenRealQueryResponse> {
//    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OpenRealQueryResponse {
//        val resultObject = json.asJsonObject
//        val resultArray = resultObject.get("datas").asJsonArray
//
//        val values = resultArray.mapNotNull { element ->
//            val details = element.asJsonObject
//            val variable = details.get("variable").asString
//            val unit = details.get("unit")?.asString
//            var value: Double? = null
//            var valueString: String? = null
//
//            val jsonElement = details.get("value")
//            if (jsonElement is JsonPrimitive) {
//                when {
//                    jsonElement.isNumber -> value = jsonElement.asDouble
//                    jsonElement.isString -> valueString = jsonElement.asString
//                }
//            }
//
//            OpenQueryResponseData(unit, variable, value, valueString)
//        }
//
//        val time = resultObject.get("time").asString
//        val deviceSN = resultObject.get("deviceSN").asString
//
//        return OpenRealQueryResponse(time, deviceSN, values)
//    }
//}

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