package dev.seankim.composeremote.compositions

import kotlinx.serialization.Serializable

@Serializable
data class ScreenDescriptor(
    val name: String,
    val variant: String? = null,
    val id: Int? = null,
    val widthDp: Int,
    val heightDp: Int,
    val actions: List<ActionDescriptor>,
)

@Serializable
data class ActionDescriptor(val id: Int, val label: String)
