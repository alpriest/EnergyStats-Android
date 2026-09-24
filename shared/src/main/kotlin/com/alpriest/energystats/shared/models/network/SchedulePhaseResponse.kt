package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class SchedulePhaseResponse(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val workMode: String,
    @Serializable(with = NumericExtraParamMapSerializer::class)
    val extraParam: Map<String, Double>?
) {
    fun extraParamValue(key: String, default: Int): Int {
        return extraParam?.get(key)?.toInt() ?: default
    }
}

/**
 * FoxESS sometimes includes non-numeric metadata such as
 * `secondWorkMode: "SelfUse"` alongside the numeric schedule parameters.
 * The app only consumes numeric parameters, so ignore those metadata entries
 * instead of rejecting the complete scheduler response.
 */
object NumericExtraParamMapSerializer : KSerializer<Map<String, Double>?> {
    private val delegate = MapSerializer(String.serializer(), Double.serializer())

    override val descriptor: SerialDescriptor = delegate.descriptor.nullable

    override fun deserialize(decoder: Decoder): Map<String, Double>? {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("NumericExtraParamMapSerializer can only be used with JSON")

        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null

        return element.jsonObject.mapNotNull { (key, value) ->
            value.jsonPrimitive.doubleOrNull?.let { key to it }
        }.toMap()
    }

    override fun serialize(encoder: Encoder, value: Map<String, Double>?) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("NumericExtraParamMapSerializer can only be used with JSON")
        jsonEncoder.encodeJsonElement(
            value?.let { jsonEncoder.json.encodeToJsonElement(delegate, it) } ?: JsonNull
        )
    }
}

@Serializable
data class SchedulePhaseRequest(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val workMode: String,
    val extraParam: Map<String, Double>?
)
