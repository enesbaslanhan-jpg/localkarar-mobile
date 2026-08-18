package com.localkarar.app.network.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

object StringListSerializer : KSerializer<List<String>> {
    private val delegate = ListSerializer(String.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: List<String>) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<String> {
        return if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            if (element is JsonPrimitive && element.isString) {
                // Try parsing the string as JSON
                try {
                    val parsed = Json.parseToJsonElement(element.content)
                    if (parsed is JsonArray) {
                        parsed.map { it.let { if (it is JsonPrimitive) it.content else it.toString() } }
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            } else if (element is JsonArray) {
                element.map { if (it is JsonPrimitive) it.content else it.toString() }
            } else {
                emptyList()
            }
        } else {
            delegate.deserialize(decoder)
        }
    }
}
