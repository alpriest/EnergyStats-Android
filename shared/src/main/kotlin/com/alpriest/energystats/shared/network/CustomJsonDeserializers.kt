
import com.alpriest.energystats.shared.models.network.OpenQueryResponseData
import com.alpriest.energystats.shared.models.network.OpenRealQueryResponse
import com.alpriest.energystats.shared.models.network.OpenReportResponse
import com.alpriest.energystats.shared.models.network.OpenReportResponseData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

object OpenReportResponseSerializer : KSerializer<OpenReportResponse> {
    override fun deserialize(decoder: Decoder): OpenReportResponse {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("OpenReportResponseSerializer can only be used with JSON")

        val obj = jsonDecoder.decodeJsonElement().asJsonObjectOrNull()
            ?: throw SerializationException("Expected a JSON object for OpenReportResponse")

        val unit = obj["unit"]?.jsonPrimitive?.contentOrNull
            ?: throw SerializationException("Missing 'unit' in OpenReportResponse")

        val variable = obj["variable"]?.jsonPrimitive?.contentOrNull
            ?: throw SerializationException("Missing 'variable' in OpenReportResponse")

        val values = obj["values"]
            ?.jsonArray
            ?.mapIndexed { index, valueEl ->
                val value = valueEl.jsonPrimitive.double
                OpenReportResponseData(index = index + 1, value = value)
            }
            ?: emptyList()

        return OpenReportResponse(
            variable = variable,
            unit = unit,
            values = values
        )
    }

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("OpenReportResponse") {
            // You can leave descriptor minimal because we’re parsing manually.
        }

    override fun serialize(encoder: Encoder, value: OpenReportResponse) {
        throw SerializationException("OpenReportResponseSerializer is decode-only")
    }
}

object OpenRealQueryResponseDeserializer : KSerializer<OpenRealQueryResponse> {
    override fun deserialize(decoder: Decoder): OpenRealQueryResponse {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("OpenRealQueryResponseDeserializer can only be used with JSON")

        val obj = jsonDecoder.decodeJsonElement().asJsonObjectOrNull()
            ?: throw SerializationException("Expected a JSON object for OpenRealQueryResponse")

        val resultArray = obj["datas"]?.jsonArray
            ?: throw SerializationException("Expected a JSON object for OpenRealQueryResponse.datas")

        val values = resultArray.mapIndexedNotNull { index, element ->
            val details = element.asJsonObjectOrNull()
                ?: throw SerializationException("Expected a JSON object for OpenRealQueryResponse.details[$index]")
            val variable = details["variable"]?.jsonPrimitive?.contentOrNull
                ?: throw SerializationException("Missing 'variable' in OpenRealQueryResponse.details[$index]")
            val unit = details["unit"]?.jsonPrimitive?.contentOrNull
                ?: throw SerializationException("Missing 'unit' in OpenRealQueryResponse.details[$index]")
            var value: Double? = null
            var valueString: String? = null
            val jsonElement = details["value"]
            if (jsonElement is JsonPrimitive) {
                when {
                    jsonElement.isString -> valueString = jsonElement.toString()
                    else -> value = jsonElement.double
                }
            }

            OpenQueryResponseData(unit, variable, value, valueString)
        }

        val time = obj["time"]?.jsonPrimitive?.contentOrNull
            ?: throw SerializationException("Missing 'time' in OpenRealQueryResponse")
        val deviceSN = obj["deviceSN"]?.jsonPrimitive?.contentOrNull
            ?: throw SerializationException("Missing 'deviceSN' in OpenRealQueryResponse")

        return OpenRealQueryResponse(time, deviceSN, values)
    }

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("OpenRealQueryResponse") {
            // You can leave descriptor minimal because we’re parsing manually.
        }

    override fun serialize(encoder: Encoder, value: OpenRealQueryResponse) {
        throw SerializationException("OpenRealQueryResponse is decode-only")
    }
}

/** Helpers */

private fun JsonElement.asJsonObjectOrNull(): JsonObject? =
    this as? JsonObject