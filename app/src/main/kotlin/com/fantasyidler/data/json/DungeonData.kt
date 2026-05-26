package com.fantasyidler.data.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DungeonData(
    val name: String,
    @SerialName("display_name") val displayName: String,
    val description: String,
    @SerialName("recommended_level") val recommendedLevel: Int,
    @SerialName("encounter_rate") val encounterRate: Double,
    @SerialName("enemy_spawns") val enemySpawns: List<EnemySpawn>,
    @SerialName("primary_type") val primaryType: String? = null,
)

@Serializable
data class EnemySpawn(
    val enemy: String,
    val weight: Int,
)
