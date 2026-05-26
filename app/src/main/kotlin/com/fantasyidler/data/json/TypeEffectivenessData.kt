package com.fantasyidler.data.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Parsed content of `assets/data/type_effectiveness.json`.
 *
 * The plan originally included a separate `types: List<String>` field alongside
 * [wheelOrder], but it was intentionally omitted here — [wheelOrder] already IS
 * the canonical ordered list of all valid types, so a duplicate field would be
 * a second source of truth that could drift. [TypeRegistry.isValidType] uses
 * [wheelOrder] directly for validation.
 */
@Serializable
data class TypeEffectivenessData(
    @SerialName("wheel_order") val wheelOrder: List<String>,
    @SerialName("strong_multiplier") val strongMultiplier: Float,
    @SerialName("weak_multiplier") val weakMultiplier: Float,
    @SerialName("neutral_multiplier") val neutralMultiplier: Float,
)
