package com.dhkim139.feature.navigationoption.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


internal open class LenientEnumSerializer<T : Enum<T>>(
    serialName: String,
    private val values: Array<T>,
    private val fallback: T,
) : KSerializer<T> {

    override val descriptor =
        PrimitiveSerialDescriptor(serialName, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): T {
        val raw = decoder.decodeString()
        return values.firstOrNull { it.name.equals(raw, ignoreCase = true) }
            ?: fallback
    }

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(value.name)
    }
}

internal object ParkingTagSerializer : LenientEnumSerializer<ParkingTag>(
    "ParkingTag", ParkingTag.entries.toTypedArray(), ParkingTag.UNKNOWN
)


internal object ParkingCategorySerializer : KSerializer<ParkingCategory> {
    private val delegate = LenientEnumSerializer(
        "ParkingCategory", ParkingCategory.entries.toTypedArray(), ParkingCategory.ETC
    )
    override val descriptor: SerialDescriptor get() = delegate.descriptor
    override fun deserialize(decoder: Decoder): ParkingCategory = delegate.deserialize(decoder)
    override fun serialize(encoder: Encoder, value: ParkingCategory) = delegate.serialize(encoder, value)
}

internal object CapacityLevelSerializer : KSerializer<CapacityLevel> {
    private val delegate = LenientEnumSerializer(
        "CapacityLevel", CapacityLevel.entries.toTypedArray(), CapacityLevel.UNKNOWN
    )
    override val descriptor: SerialDescriptor get() = delegate.descriptor
    override fun deserialize(decoder: Decoder): CapacityLevel = delegate.deserialize(decoder)
    override fun serialize(encoder: Encoder, value: CapacityLevel) = delegate.serialize(encoder, value)
}
