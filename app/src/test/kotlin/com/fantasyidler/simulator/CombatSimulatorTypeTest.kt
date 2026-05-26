package com.fantasyidler.simulator

import com.fantasyidler.data.json.DungeonData
import com.fantasyidler.data.json.EnemyCombatStats
import com.fantasyidler.data.json.EnemyData
import com.fantasyidler.data.json.EnemyDefensiveStats
import com.fantasyidler.data.json.EnemySpawn
import com.fantasyidler.data.json.TypeEffectivenessData
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Verifies that CombatSimulator applies TypeRegistry multipliers to player damage.
 *
 * Strategy: run the same dungeon twice with identical player stats but different
 * weapon types. A strong-type weapon (fire vs ground enemy) should produce higher
 * total player damage than a null-type weapon over the same session.
 *
 * Uses a fixed Random seed indirectly by running enough ticks that the law of large
 * numbers dominates — total damage over 60 frames with 25 ticks each is stable.
 */
class CombatSimulatorTypeTest {

    private val groundEnemy = EnemyData(
        name = "test_goblin",
        displayName = "Test Goblin",
        hp = 5,
        combatStats = EnemyCombatStats(
            attackLevel = 1, strengthLevel = 1, defenseLevel = 1,
            attackBonus = 0, strengthBonus = 0,
        ),
        defensiveStats = EnemyDefensiveStats(
            attackDefense = 0, strengthDefense = 0,
            rangedDefense = 0, magicDefense = 0,
        ),
        xpDrops = mapOf("combat" to 10),
        type = "ground",
    )

    private val dungeon = DungeonData(
        name = "test_dungeon",
        displayName = "Test Dungeon",
        description = "",
        recommendedLevel = 1,
        encounterRate = 1.0,
        enemySpawns = listOf(EnemySpawn(enemy = "test_goblin", weight = 1)),
        primaryType = "ground",
    )

    private val enemies = mapOf("test_goblin" to groundEnemy)

    @Before
    fun setUp() {
        TypeRegistry.init(
            TypeEffectivenessData(
                wheelOrder = listOf("fire", "dark", "ice", "light", "ground", "lightning", "air", "water"),
                strongMultiplier = 1.5f,
                weakMultiplier = 0.67f,
                neutralMultiplier = 1.0f,
            )
        )
    }

    /**
     * Run the simulation [runs] times and return the total damage across all runs.
     * Multiple runs average out Random(System.nanoTime()) variance, making comparisons
     * across weapon types stable even without a fixed seed.
     */
    private fun totalPlayerDamage(weaponType: String?, runs: Int = 10): Long {
        var total = 0L
        repeat(runs) {
            val result = CombatSimulator.simulateDungeon(
                dungeon = dungeon,
                enemies = enemies,
                playerAttack = 99,
                playerStrength = 99,
                playerDefence = 99,
                playerHp = 99,
                weaponAttackBonus = 50,
                weaponStrengthBonus = 50,
                playerWeaponType = weaponType,
            )
            total += result.frames.sumOf { frame -> frame.playerHits.sum() }
        }
        return total
    }

    @Test
    fun `ice weapon deals more damage to ground enemy than untyped weapon`() {
        val iceTotal     = totalPlayerDamage("ice")
        val untypedTotal = totalPlayerDamage(null)
        assertTrue(
            "Expected ice (strong vs ground) total=$iceTotal > untyped total=$untypedTotal",
            iceTotal > untypedTotal,
        )
    }

    @Test
    fun `lightning weapon deals less damage to ground enemy than untyped weapon`() {
        val lightningTotal = totalPlayerDamage("lightning")
        val untypedTotal   = totalPlayerDamage(null)
        assertTrue(
            "Expected lightning (weak vs ground) total=$lightningTotal < untyped total=$untypedTotal",
            lightningTotal < untypedTotal,
        )
    }

    @Test
    fun `neutral type weapon deals same damage as untyped weapon`() {
        val neutralTotal = totalPlayerDamage("neutral")
        val untypedTotal = totalPlayerDamage(null)
        val ratio = neutralTotal.toDouble() / untypedTotal.toDouble()
        assertTrue("Expected neutral ≈ untyped, got ratio=$ratio", ratio in 0.90..1.10)
    }
}
