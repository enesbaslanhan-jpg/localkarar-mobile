package com.localkarar.app.core

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.contentOrNull

fun JsonElement?.displayValue(): String {
    return when (this) {
        null, is JsonNull -> ""
        is JsonPrimitive -> when {
            isString -> contentOrNull ?: ""
            doubleOrNull != null -> LkFormatting.formatNumber(doubleOrNull)
            else -> contentOrNull ?: ""
        }
        is JsonArray -> joinToString(", ") { it.displayValue() }
        is JsonObject -> keys.joinToString(", ") { key -> key.replace('_', ' ').replaceFirstChar { it.uppercase() } }
    }
}

fun keyLabel(key: String): String {
    return key.replace('_', ' ').replaceFirstChar { it.uppercase() }
}