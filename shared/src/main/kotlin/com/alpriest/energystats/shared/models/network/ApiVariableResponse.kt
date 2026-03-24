package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = ApiVariableKSerializer::class)
data class ApiVariableArray(
    val array: List<ApiVariable>
)

@Serializable
data class ApiVariable(
    val name: String,
    val variable: String,
    val unit: String?
)

object ApiVariableKSerializer : KSerializer<ApiVariableArray> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ApiVariableArray", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ApiVariableArray {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("OpenApiVariableKSerializer can only be used with JSON")

        val resultArray = jsonDecoder.decodeJsonElement() as JsonArray

        val apiVariables = resultArray.mapNotNull { resultElement ->
            resultElement.jsonObject.entries.firstOrNull()?.let { (variableName, detailsElement) ->
                val details = detailsElement.jsonObject
                val unit = details["unit"]?.jsonPrimitive?.contentOrNull
                val nameTranslations = details["name"] as? JsonObject
                val name = nameTranslations
                    ?.entries
                    ?.firstOrNull { it.key == "en" }
                    ?.value
                    ?.jsonPrimitive
                    ?.content
                    ?: ""

                ApiVariable(name = name, variable = variableName, unit = unit)
            }
        }

        return ApiVariableArray(array = apiVariables)
    }

    override fun serialize(encoder: Encoder, value: ApiVariableArray) {
        error("OpenApiVariableKSerializer is only intended for deserialization")
    }
}
