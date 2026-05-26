# Phase 2 — Combat Means Something: Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire the Phase 1 type wheel into dungeon combat so player weapon type vs enemy type produces real damage multipliers, and surface that information in the combat UI.

**Architecture:** Add `type` to `EnemyData` and `primaryType` to `DungeonData`; assign thematic types to all 23 enemies and 12 dungeons. Add `type` to `EquipmentData` (null default). Modify `CombatSimulator.simulateDungeon()` to scale `playerMaxHit` by `TypeRegistry.multiplier(weaponType, enemyType)` per frame. Add type chips to the dungeon list and enemy spawn display in `CombatScreen`.

**Tech Stack:** Kotlin, kotlinx.serialization, JUnit 4, Jetpack Compose Material3

---

## Type Assignment Reference

### Enemy types (assign in Task 1)

| Enemy key | Type | Reasoning |
|---|---|---|
| chicken | ground | Farmyard animal |
| sheep | ground | Farmyard animal |
| cow | ground | Farmyard animal |
| giant_rat | ground | Cave rodent |
| goblin | ground | Cave dwellers |
| spider | ground | Cave creature |
| wild_dog | ground | Natural predator |
| orc_warrior | ground | Brutish earth creature |
| troll | ground | Mountain creature |
| imp | fire | Fire demon; drops fire runes |
| hellhound | fire | Hell creature |
| demon | fire | Lesser demon; fire-themed drops |
| fire_giant | fire | Explicit |
| skeleton | dark | Undead |
| zombie | dark | Undead |
| dark_wizard | dark | Dark magic |
| lich | dark | Undead spellcaster |
| dragon | fire | Classic fire-breathing dragon |
| knight | light | Chivalric archetype; honor/purity |
| ancient_guardian | light | Temple guardian; divine wisdom |
| guard | neutral | Generic human soldier |
| rogue | neutral | Generic human thief |
| bandit | neutral | Generic human bandit |

### Dungeon primary types (assign in Task 2)

| Dungeon file | Primary type | Enemies |
|---|---|---|
| farm | ground | chicken, sheep, cow |
| goblin_cave | ground | giant_rat, goblin |
| spider_den | ground | spider, wild_dog |
| imp_cavern | fire | imp, giant_rat |
| bandit_camp | neutral | rogue, bandit |
| undead_crypt | dark | skeleton, zombie, dark_wizard |
| fortress_ruins | neutral | guard, knight, dark_wizard (mixed) |
| orc_stronghold | ground | orc_warrior, troll |
| volcanic_depths | fire | hellhound, demon |
| infernal_stronghold | fire | fire_giant, demon, hellhound |
| dragon_lair | fire | dragon, demon, knight |
| ancient_temple | light | ancient_guardian, lich, dragon |

---

## File Map

| File | Change |
|---|---|
| `app/src/main/assets/data/enemies.json` | Add `"type"` field to all 23 enemies |
| `app/src/main/assets/data/dungeons/*.json` | Add `"primary_type"` field to all 12 files |
| `app/src/main/kotlin/com/fantasyidler/data/json/EnemyData.kt` | Add `val type: String? = null` |
| `app/src/main/kotlin/com/fantasyidler/data/json/DungeonData.kt` | Add `val primaryType: String? = null` |
| `app/src/main/kotlin/com/fantasyidler/data/json/EquipmentData.kt` | Add `val type: String? = null` |
| `app/src/main/kotlin/com/fantasyidler/simulator/CombatSimulator.kt` | Add `playerWeaponType` param; apply multiplier to `playerMaxHit` per frame |
| `app/src/test/kotlin/com/fantasyidler/simulator/CombatSimulatorTypeTest.kt` | New: tests that strong type increases total player damage |
| `app/src/main/kotlin/com/fantasyidler/ui/screen/CombatScreen.kt` | Add type chip to dungeon cards and enemy spawn rows |

---

## Task 1: Add type field to enemies

**Files:**
- Modify: `app/src/main/assets/data/enemies.json`
- Modify: `app/src/main/kotlin/com/fantasyidler/data/json/EnemyData.kt`

- [ ] **Step 1: Add `val type: String? = null` to `EnemyData`**

In `EnemyData.kt`, add after `alwaysDrops`:

```kotlin
@Serializable
data class EnemyData(
    val name: String,
    @SerialName("display_name") val displayName: String,
    val hp: Int,
    @SerialName("combat_stats") val combatStats: EnemyCombatStats,
    @SerialName("defensive_stats") val defensiveStats: EnemyDefensiveStats,
    @SerialName("xp_drops") val xpDrops: Map<String, Int>,
    @SerialName("drop_table") val dropTable: List<DropEntry> = emptyList(),
    @SerialName("always_drops") val alwaysDrops: List<AlwaysDrop> = emptyList(),
    val type: String? = null,
)
```

- [ ] **Step 2: Add `"type"` to every enemy in `enemies.json`**

Add `"type": "<value>"` as the last field of each enemy object. Use the reference table above. Example for `giant_rat`:

```json
"giant_rat": {
  "name": "giant_rat",
  "display_name": "Giant Rat",
  "hp": 10,
  "combat_stats": { ... },
  "defensive_stats": { ... },
  "xp_drops": { "combat": 30 },
  "drop_table": [...],
  "always_drops": [...],
  "type": "ground"
}
```

Apply to all 23 enemies per the reference table.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/assets/data/enemies.json \
        app/src/main/kotlin/com/fantasyidler/data/json/EnemyData.kt
git commit -m "phase-2 task 1: add type field to all enemies"
```

---

## Task 2: Add primary_type field to dungeons

**Files:**
- Modify: `app/src/main/assets/data/dungeons/*.json` (all 12 files)
- Modify: `app/src/main/kotlin/com/fantasyidler/data/json/DungeonData.kt`

- [ ] **Step 1: Add `val primaryType: String? = null` to `DungeonData`**

```kotlin
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
```

- [ ] **Step 2: Add `"primary_type"` to each dungeon JSON file**

Add `"primary_type": "<value>"` after `"encounter_rate"` in each file. Use the reference table above. Example for `goblin_cave.json`:

```json
{
  "name": "goblin_cave",
  "display_name": "Goblin Cave",
  "description": "A dark cave infested with goblins and giant rats. A good place for new adventurers to train.",
  "recommended_level": 3,
  "encounter_rate": 0.22,
  "primary_type": "ground",
  "enemy_spawns": [
    { "enemy": "giant_rat", "weight": 3 },
    { "enemy": "goblin", "weight": 7 }
  ]
}
```

Apply to all 12 dungeon files per the reference table.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/assets/data/dungeons/ \
        app/src/main/kotlin/com/fantasyidler/data/json/DungeonData.kt
git commit -m "phase-2 task 2: add primary_type to all dungeons"
```

---

## Task 3: Add type field to EquipmentData

**Files:**
- Modify: `app/src/main/kotlin/com/fantasyidler/data/json/EquipmentData.kt`

- [ ] **Step 1: Add `val type: String? = null` to `EquipmentData`**

Add as the last field:

```kotlin
@Serializable
data class EquipmentData(
    val name: String,
    @SerialName("display_name")           val displayName: String,
    val slot: String,
    @SerialName("combat_style")           val combatStyle: String? = null,
    val description: String = "",
    @SerialName("attack_bonus")           val attackBonus: Int = 0,
    @SerialName("strength_bonus")         val strengthBonus: Int = 0,
    @SerialName("defense_bonus")          val defenseBonus: Int = 0,
    val requirements: Map<String, Int> = emptyMap(),
    @SerialName("infinite_runes")          val infiniteRunes: String? = null,
    @SerialName("mining_efficiency")      val miningEfficiency: Float? = null,
    @SerialName("woodcutting_efficiency") val woodcuttingEfficiency: Float? = null,
    @SerialName("fishing_efficiency")     val fishingEfficiency: Float? = null,
    @SerialName("farming_efficiency")     val farmingEfficiency: Float? = null,
    val type: String? = null,
)
```

No `equipment.json` changes needed yet — types default to null. Weapon type assignment is Phase 3 (gear inherits type from crafting materials). The field being present now means Phase 3 can populate it without touching the data class.

- [ ] **Step 2: Commit**

```bash
git add app/src/main/kotlin/com/fantasyidler/data/json/EquipmentData.kt
git commit -m "phase-2 task 3: add type field to EquipmentData (null default)"
```

---

## Task 4: Write failing tests for CombatSimulator type multiplier

**Files:**
- Create: `app/src/test/kotlin/com/fantasyidler/simulator/CombatSimulatorTypeTest.kt`

- [ ] **Step 1: Create the test file**

```kotlin
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

    private fun totalPlayerDamage(weaponType: String?): Int {
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
        return result.frames.sumOf { frame -> frame.playerHits.sum() }
    }

    @Test
    fun `fire weapon deals more damage to ground enemy than untyped weapon`() {
        val fireTotal    = totalPlayerDamage("fire")   // fire beats ground (2 steps CW)
        val untypedTotal = totalPlayerDamage(null)
        assertTrue(
            "Expected fire (strong vs ground) total=$fireTotal > untyped total=$untypedTotal",
            fireTotal > untypedTotal,
        )
    }

    @Test
    fun `water weapon deals less damage to ground enemy than untyped weapon`() {
        val waterTotal   = totalPlayerDamage("water")  // water loses to ground (2 steps CCW)
        val untypedTotal = totalPlayerDamage(null)
        assertTrue(
            "Expected water (weak vs ground) total=$waterTotal < untyped total=$untypedTotal",
            waterTotal < untypedTotal,
        )
    }

    @Test
    fun `neutral type weapon deals same damage as untyped weapon`() {
        val neutralTotal = totalPlayerDamage("neutral")
        val untypedTotal = totalPlayerDamage(null)
        // Both return 1.0x from TypeRegistry. Two independent RNG runs have ~4.5% natural
        // variance each over 1500 ticks, so combined variance can reach ~9%. Use 15% to
        // avoid flakiness without widening enough to miss a real regression.
        val ratio = neutralTotal.toDouble() / untypedTotal.toDouble()
        assertTrue("Expected neutral ≈ untyped, got ratio=$ratio", ratio in 0.85..1.15)
    }
}
```

- [ ] **Step 2: Run the tests — confirm they fail**

```bash
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=~/Android/Sdk \
  ./gradlew testDebugUnitTest --tests "com.fantasyidler.simulator.CombatSimulatorTypeTest" 2>&1 | tail -20
```

Expected: **FAIL** — `playerWeaponType` parameter does not exist on `simulateDungeon` yet.

---

## Task 5: Implement type multiplier in CombatSimulator

**Files:**
- Modify: `app/src/main/kotlin/com/fantasyidler/simulator/CombatSimulator.kt`

- [ ] **Step 1: Add `playerWeaponType` parameter to `simulateDungeon`**

Add `playerWeaponType: String? = null` as the last parameter of `simulateDungeon` (after `potionBonuses` and `availableArrows`):

```kotlin
fun simulateDungeon(
    dungeon: DungeonData,
    enemies: Map<String, EnemyData>,
    playerAttack: Int,
    playerStrength: Int,
    playerDefence: Int,
    playerHp: Int = 10,
    weaponAttackBonus: Int = 0,
    weaponStrengthBonus: Int = 0,
    combatStyle: String = "melee",
    playerRanged: Int = 1,
    playerMagic: Int = 1,
    arrowStrengthBonus: Int = 0,
    spellMaxHit: Int = 0,
    agilityLevel: Int = 1,
    petBoostPct: Int = 0,
    equippedFood: Map<String, Int> = emptyMap(),
    foodHealValues: Map<String, Int> = emptyMap(),
    potionBonuses: Map<String, Int> = emptyMap(),
    availableArrows: Map<String, Int> = emptyMap(),
    playerWeaponType: String? = null,
): SkillSimulator.Result {
```

- [ ] **Step 2: Apply the type multiplier per frame**

In the `for (minute in 1..60)` loop, after `val enemy = enemies[enemyKey] ?: continue` and after the `when (combatStyle)` block that sets `playerMaxHit`, add:

```kotlin
// Scale player max hit by type effectiveness (weapon type vs this enemy's type).
// TypeRegistry returns 1.0 if either side is null or "neutral".
val typeMult = TypeRegistry.multiplier(playerWeaponType, enemy.type)
val effectivePlayerMaxHit = (playerMaxHit * typeMult).roundToInt().coerceAtLeast(1)
```

Add `import kotlin.math.roundToInt` at the top of the file if not already present.

- [ ] **Step 3: Replace `playerMaxHit` with `effectivePlayerMaxHit` in the tick loop**

Find the three places in `repeat(TICKS_PER_FRAME)` that reference `playerMaxHit` in damage rolls and replace them with `effectivePlayerMaxHit`:

```kotlin
// ranged branch:
if (rnd.nextDouble() < playerHitChance) rnd.nextInt(0, effectivePlayerMaxHit + 1) else 0

// ranged out-of-arrows: stays 0, no change needed

// melee/magic branch:
if (rnd.nextDouble() < playerHitChance) rnd.nextInt(0, effectivePlayerMaxHit + 1) else 0
```

The existing code has two hit-roll sites inside the `when` inside `repeat`. Replace both `playerMaxHit` references inside those rolls with `effectivePlayerMaxHit`.

- [ ] **Step 4: Run the tests — confirm they pass**

```bash
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=~/Android/Sdk \
  ./gradlew testDebugUnitTest --tests "com.fantasyidler.simulator.CombatSimulatorTypeTest" 2>&1 | tail -15
```

Expected: **BUILD SUCCESSFUL**, 3 tests pass.

- [ ] **Step 5: Run all tests to confirm no regressions**

```bash
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=~/Android/Sdk \
  ./gradlew testDebugUnitTest 2>&1 | tail -15
```

Expected: **BUILD SUCCESSFUL**, all tests pass (TypeRegistryTest + CombatSimulatorTypeTest).

- [ ] **Step 6: Commit**

```bash
git add app/src/main/kotlin/com/fantasyidler/simulator/CombatSimulator.kt \
        app/src/test/kotlin/com/fantasyidler/simulator/CombatSimulatorTypeTest.kt
git commit -m "phase-2 task 4+5: apply type multiplier in CombatSimulator

Add playerWeaponType param to simulateDungeon (null default, backward compat).
Per-frame: scale playerMaxHit by TypeRegistry.multiplier(weaponType, enemy.type).
3 new tests verify strong/weak/neutral type multiplier paths."
```

---

## Task 6: Surface type information in the Combat UI

**Files:**
- Modify: `app/src/main/kotlin/com/fantasyidler/ui/screen/CombatScreen.kt`

The goal is two targeted additions:
1. Show the dungeon's `primary_type` as a small chip on each dungeon list card
2. Show each enemy's `type` in the spawn list shown in the dungeon detail sheet

Search for where dungeon items are rendered in `CombatScreen.kt` (look for `DungeonData` usage and the list that shows dungeons). Add a type chip beneath the dungeon name. Search for where `EnemySpawn` or enemy names are rendered in the detail sheet and add the enemy type inline.

- [ ] **Step 1: Add a `TypeChip` composable at the top of `CombatScreen.kt`**

Add this composable near the top of the file, after the imports:

```kotlin
@Composable
private fun TypeChip(type: String?, modifier: Modifier = Modifier) {
    if (type == null || type == "neutral") return
    val label = type.replaceFirstChar { it.uppercase() }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}
```

- [ ] **Step 2: Add `TypeChip` to the dungeon list item**

Find the composable that renders a single dungeon row in the list (search for where `dungeon.displayName` is rendered inside a `LazyColumn` or `Column`). Add `TypeChip(dungeon.primaryType)` directly below the dungeon's display name:

```kotlin
Text(
    text = dungeon.displayName,
    style = MaterialTheme.typography.bodyLarge,
    fontWeight = FontWeight.Bold,
)
TypeChip(dungeon.primaryType, modifier = Modifier.padding(top = 2.dp))
```

- [ ] **Step 3: Add enemy type to the spawn list in the dungeon detail sheet**

Find where enemy spawns are rendered in the dungeon detail (search for `enemySpawns` or where enemy names are shown). Add the enemy type inline after the enemy display name:

```kotlin
// Find the enemy data to get the type:
val enemy = enemies[spawn.enemy]  // enemies: Map<String, EnemyData> from viewmodel
Row(verticalAlignment = Alignment.CenterVertically) {
    Text(
        text = enemy?.displayName ?: spawn.enemy,
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(Modifier.width(6.dp))
    TypeChip(enemy?.type)
}
```

The `enemies` map is already available in the CombatScreen via the viewmodel or passed as a parameter — check how the dungeon detail sheet is currently receiving dungeon data and follow the same pattern to thread `enemies` through.

- [ ] **Step 4: Build the app to verify no compile errors**

```bash
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=~/Android/Sdk \
  ./gradlew assembleDebug 2>&1 | tail -20
```

Expected: **BUILD SUCCESSFUL**

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/com/fantasyidler/ui/screen/CombatScreen.kt
git commit -m "phase-2 task 6: surface type info in combat UI

Add TypeChip composable. Show dungeon primary_type below dungeon name
in the dungeon list. Show enemy type inline in the spawn list inside
the dungeon detail sheet."
```

---

## Task 7: Wire playerWeaponType through to simulateDungeon call site

**Files:**
- Modify: `app/src/main/kotlin/com/fantasyidler/ui/viewmodel/CombatViewModel.kt` (or wherever `CombatSimulator.simulateDungeon` is called — search for `simulateDungeon`)

- [ ] **Step 1: Find the call site**

```bash
grep -rn "simulateDungeon" app/src/main/kotlin/
```

- [ ] **Step 2: Pass the equipped weapon's type**

At the call site, the equipped weapon is already available (as `equippedWeapon: EquipmentData?` in `CombatUiState`). Add `playerWeaponType = equippedWeapon?.type` to the `simulateDungeon` call:

```kotlin
CombatSimulator.simulateDungeon(
    dungeon = dungeon,
    enemies = gameData.enemies,
    playerAttack = ...,
    // ... all existing params unchanged ...
    playerWeaponType = equippedWeapon?.type,
)
```

- [ ] **Step 3: Build and run all tests**

```bash
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=~/Android/Sdk \
  ./gradlew testDebugUnitTest 2>&1 | tail -10
```

Expected: **BUILD SUCCESSFUL**, all tests pass.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/com/fantasyidler/ui/viewmodel/CombatViewModel.kt
git commit -m "phase-2 task 7: pass weapon type through to CombatSimulator

Passes equippedWeapon?.type as playerWeaponType in the simulateDungeon
call. Null for all current weapons (no types assigned yet) — multiplier
is 1.0 until Phase 3 assigns weapon types via crafting materials."
```

---

## Task 8: Final verification

- [ ] **Step 1: Run full test suite**

```bash
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=~/Android/Sdk \
  ./gradlew testDebugUnitTest 2>&1 | tail -15
```

Expected: all tests pass.

- [ ] **Step 2: Build debug APK**

```bash
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=~/Android/Sdk \
  ./gradlew assembleDebug 2>&1 | tail -10
```

Expected: **BUILD SUCCESSFUL**

- [ ] **Step 3: Push branch**

```bash
git push origin phase-2-combat
```

---

## Notes for Phase 3

- `EquipmentData.type` is already scaffolded (null for all current items). Phase 3 populates it by computing type from the primary crafting material.
- `enemy.type` and `dungeon.primaryType` are live in the simulator — no Phase 3 changes needed to the combat engine for basic type-vs-type damage.
- The `TypeChip` composable will be reused in Phase 3 when item types appear in inventory/equipment screens.
