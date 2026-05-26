package com.fantasyidler.data.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TypeEffectivenessData(
    @SerialName("wheel_order") val wheelOrder: List<String>,
    @SerialName("strong_multiplier") val strongMultiplier: Float,
    @SerialName("weak_multiplier") val weakMultiplier: Float,
    @SerialName("neutral_multiplier") val neutralMultiplier: Float,
)
