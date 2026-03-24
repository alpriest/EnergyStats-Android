package com.alpriest.energystats.shared.models.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class PagedDataLoggerListResponse(
    val total: Int,
    val data: List<DataLoggerResponse>
)

@Serializable
data class DataLoggerResponse(
    val moduleSN: String,
    val stationID: String,
    val status: DataLoggerStatus,
    val signal: Int
)

@Serializable(with = DataLoggerStatusSerializer::class)
enum class DataLoggerStatus(val value: Int) {
    UNKNOWN(0),
    ONLINE(1),
    OFFLINE(2)
}

data class DataLoggerListRequest(
    val pageSize: Int = 20,
    val currentPage: Int = 1,
    val total: Int = 0,
    val condition: Condition = Condition()
) {
    data class Condition(
        val communication: Int = 0,
        val moduleSN: String = "",
        val moduleType: String = ""
    )
}

object DataLoggerStatusSerializer : KSerializer<DataLoggerStatus> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DataLoggerStatus", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): DataLoggerStatus {
        val value = decoder.decodeInt()
        return DataLoggerStatus.entries.firstOrNull { it.value == value }
            ?: DataLoggerStatus.UNKNOWN
    }

    override fun serialize(encoder: Encoder, value: DataLoggerStatus) {
        encoder.encodeInt(value.value)
    }
}