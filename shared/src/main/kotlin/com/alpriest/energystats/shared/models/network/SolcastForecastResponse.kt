package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Date

@Serializable
data class SolcastForecastResponseList(
    val forecasts: List<SolcastForecastResponse>
)

@Serializable
data class SolcastForecastResponse(
    @SerialName("pv_estimate")
    val pvEstimate: Double,
    @SerialName("pv_estimate10")
    val pvEstimate10: Double,
    @SerialName("pv_estimate90")
    val pvEstimate90: Double,
    @SerialName("period_end")
    @Serializable(with = DateSerializer::class)
    val periodEnd: Date,
    val period: String
)

fun Date.toHalfHourOfDay(): Int {
    val calendar = Calendar.getInstance().apply { time = this@toHalfHourOfDay }
    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val minutes = calendar.get(Calendar.MINUTE)

    return hours * 2 + if (minutes >= 30) 1 else 0
}

object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        // Always encode as an ISO-8601 instant (UTC), e.g. 2026-01-26T12:30:00Z
        encoder.encodeString(value.toInstant().toString())
    }

    override fun deserialize(decoder: Decoder): Date {
        val text = decoder.decodeString().trim()

        // Solcast typically returns ISO-8601 with a timezone/offset (often a trailing 'Z').
        // Be tolerant of a few formats.
        val instant = try {
            Instant.parse(text)
        } catch (_: DateTimeParseException) {
            try {
                OffsetDateTime.parse(text).toInstant()
            } catch (_: DateTimeParseException) {
                // If no zone information is present, treat as UTC.
                LocalDateTime.parse(text).toInstant(ZoneOffset.UTC)
            }
        }

        return Date.from(instant)
    }
}