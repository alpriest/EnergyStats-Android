package com.alpriest.energystats.shared.models

import com.alpriest.energystats.shared.models.network.SolcastSiteResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable(with = SolcastSettingsSerializer::class)
data class SolcastSettings(
    val apiKey: String? = null,
    val sites: List<SolcastSite> = emptyList()
) {
    companion object {
        val defaults: SolcastSettings = SolcastSettings(apiKey = null, sites = emptyList())
        val demo: SolcastSettings = SolcastSettings(
            apiKey = "123", sites = listOf(SolcastSite.preview())
        )
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = SolcastSettings::class)
object SolcastSettingsSerializer : KSerializer<SolcastSettings> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("SolcastSettings") {
        element<String?>("apiKey", isOptional = true)
        element<List<SolcastSite>>("sites", isOptional = true)
    }

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: SolcastSettings) {
        encoder.encodeStructure(descriptor) {
            value.apiKey?.let { encodeStringElement(descriptor, 0, it) }
            encodeSerializableElement(descriptor, 1, ListSerializer(SolcastSiteSerializer), value.sites)
        }
    }

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): SolcastSettings {
        var apiKey: String? = null
        var sites: List<SolcastSite> = emptyList()

        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> apiKey = decodeNullableSerializableElement(descriptor, index, serializer<String>().nullable)
                    1 -> sites = decodeSerializableElement(descriptor, index, ListSerializer(SolcastSiteSerializer))
                    else -> error("Unexpected index: $index")
                }
            }
        }

        return SolcastSettings(
            apiKey = apiKey,
            sites = sites
        )
    }
}

@Serializable(with = SolcastSiteSerializer::class)
data class SolcastSite(
    val name: String,
    val resourceId: String,
    val lng: Double,
    val lat: Double,
    val azimuth: Double,
    val tilt: Double,
    val lossFactor: Double?,
    val acCapacity: Double,
    val dcCapacity: Double?,
    val installDate: LocalDate?
) {
    constructor(site: SolcastSiteResponse) : this(
        name = site.name,
        resourceId = site.resourceId,
        lng = site.longitude,
        lat = site.latitude,
        azimuth = site.azimuth,
        tilt = site.tilt,
        lossFactor = site.lossFactor,
        acCapacity = site.capacity,
        dcCapacity = site.dcCapacity,
        installDate = site.installDate?.let {
            LocalDate.parse(it.split("T")[0], DateTimeFormatter.ISO_LOCAL_DATE)
        }
    )

    companion object
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = SolcastSite::class)
object SolcastSiteSerializer : KSerializer<SolcastSite> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("SolcastSite") {
        element<String>("name")
        element<String>("resourceId")
        element<Double>("lng")
        element<Double>("lat")
        element<Double>("azimuth")
        element<Double>("tilt")
        element<Double?>("lossFactor", isOptional = true)
        element<Double>("acCapacity")
        element<Double?>("dcCapacity", isOptional = true)
        element<String?>("installDate", isOptional = true)
    }

    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: SolcastSite) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.name)
            encodeStringElement(descriptor, 1, value.resourceId)
            encodeDoubleElement(descriptor, 2, value.lng)
            encodeDoubleElement(descriptor, 3, value.lat)
            encodeDoubleElement(descriptor, 4, value.azimuth)
            encodeDoubleElement(descriptor, 5, value.tilt)
            value.lossFactor?.let { encodeDoubleElement(descriptor, 6, it) }
            encodeDoubleElement(descriptor, 7, value.acCapacity)
            value.dcCapacity?.let { encodeDoubleElement(descriptor, 8, it) }
            value.installDate?.let { encodeStringElement(descriptor, 9, it.format(DateTimeFormatter.ISO_LOCAL_DATE)) }
        }
    }

    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): SolcastSite {
        var name: String? = null
        var resourceId: String? = null
        var lng: Double? = null
        var lat: Double? = null
        var azimuth: Double? = null
        var tilt: Double? = null
        var lossFactor: Double? = null
        var acCapacity: Double? = null
        var dcCapacity: Double? = null
        var installDate: LocalDate? = null
        val jsonInput = decoder as? JsonDecoder
        val jsonObject = jsonInput?.decodeJsonElement()?.jsonObject

        if (jsonObject != null) {
            fun primitive(key: String): JsonPrimitive? = jsonObject[key] as? JsonPrimitive

            val installDate = primitive("installDate")?.contentOrNull?.let { value ->
                runCatching { LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
            }

            return SolcastSite(
                name = primitive("name")?.content ?: error("Missing name"),
                resourceId = primitive("resourceId")?.content ?: error("Missing resourceId"),
                lng = primitive("lng")?.double ?: error("Missing lng"),
                lat = primitive("lat")?.double ?: error("Missing lat"),
                azimuth = primitive("azimuth")?.double ?: error("Missing azimuth"),
                tilt = primitive("tilt")?.double ?: error("Missing tilt"),
                lossFactor = primitive("lossFactor")?.doubleOrNull,
                acCapacity = primitive("acCapacity")?.double ?: error("Missing acCapacity"),
                dcCapacity = primitive("dcCapacity")?.doubleOrNull,
                installDate = installDate
            )
        }

        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> name = decodeStringElement(descriptor, index)
                    1 -> resourceId = decodeStringElement(descriptor, index)
                    2 -> lng = decodeDoubleElement(descriptor, index)
                    3 -> lat = decodeDoubleElement(descriptor, index)
                    4 -> azimuth = decodeDoubleElement(descriptor, index)
                    5 -> tilt = decodeDoubleElement(descriptor, index)
                    6 -> lossFactor = decodeDoubleElement(descriptor, index)
                    7 -> acCapacity = decodeDoubleElement(descriptor, index)
                    8 -> dcCapacity = decodeDoubleElement(descriptor, index)
                    9 -> installDate = decodeStringElement(descriptor, index).let { value ->
                        runCatching { LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
                    }
                    else -> error("Unexpected index: $index")
                }
            }
        }

        return SolcastSite(
            name = requireNotNull(name) { "Missing name" },
            resourceId = requireNotNull(resourceId) { "Missing resourceId" },
            lng = requireNotNull(lng) { "Missing lng" },
            lat = requireNotNull(lat) { "Missing lat" },
            azimuth = requireNotNull(azimuth) { "Missing azimuth" },
            tilt = requireNotNull(tilt) { "Missing tilt" },
            lossFactor = lossFactor,
            acCapacity = requireNotNull(acCapacity) { "Missing acCapacity" },
            dcCapacity = dcCapacity,
            installDate = installDate
        )
    }
}

fun SolcastSite.Companion.preview(name: String = "Front panels"): SolcastSite {
    return SolcastSite(
        name = name,
        resourceId = "abc-123-def-456",
        lng = -2.470923,
        lat = 53.377811,
        azimuth = 134.0,
        tilt = 45.0,
        lossFactor = 0.9,
        acCapacity = 3.7,
        dcCapacity = 5.6,
        installDate = LocalDate.now()
    )
}