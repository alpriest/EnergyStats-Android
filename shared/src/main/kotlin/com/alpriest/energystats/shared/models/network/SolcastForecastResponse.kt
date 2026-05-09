package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Date
import kotlin.time.Instant

@Serializable
data class SolcastForecastResponseList(
    val forecasts: List<SolcastForecastResponse>
)

object InstantAsStringSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        val text = decoder.decodeString()

        // Solcast typically returns ISO-8601 with a timezone/offset (often a trailing 'Z').
        // Be tolerant of a few formats.
        val instant: Instant? = try {
            Instant.parseOrNull(text)
        } catch (_: DateTimeParseException) {
            try {
                OffsetDateTime.parse(text).toInstant().toKotlinInstant()
            } catch (_: DateTimeParseException) {
                // If no zone information is present, treat as UTC.
                LocalDateTime.parse(text).toInstant(ZoneOffset.UTC).toKotlinInstant()
            }
        } catch (_: IllegalStateException) {
            return Instant.DISTANT_PAST
        }

        return instant ?: Instant.DISTANT_PAST
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}

private fun java.time.Instant.toKotlinInstant(): Instant =
    Instant.fromEpochSeconds(epochSecond, nano.toLong())

@Serializable
data class SolcastForecastResponse(
    @SerialName("pv_estimate")
    val pvEstimate: Double,
    @SerialName("pv_estimate10")
    val pvEstimate10: Double,
    @SerialName("pv_estimate90")
    val pvEstimate90: Double,
    @Serializable(with = InstantAsStringSerializer::class)
    @SerialName("period_end")
    val periodEnd: Instant,
    val period: String
)

fun Date.toHalfHourOfDay(): Int {
    val calendar = Calendar.getInstance().apply { time = this@toHalfHourOfDay }
    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val minutes = calendar.get(Calendar.MINUTE)

    return hours * 2 + if (minutes >= 30) 1 else 0
}
