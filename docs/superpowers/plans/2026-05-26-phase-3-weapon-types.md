# Phase 3 — Weapon Types Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Craftable weapons inherit an elemental type from their recipe; that type merges into `EquipmentData` at app start so `CombatSimulator`'s Phase 2 multiplier fires for real.

**Architecture:** Add `element_type: String?` to smithing and fletching recipe entries in JSON. Add `elementType: String?` field to `SmithingRecipe` and `FletchingRecipe` data classes. In `GameDataRepository`, replace the `equipment` lazy loader with one that merges recipe element types into the static `EquipmentData` map at startup. Extract `TypeChip` to a shared component and show weapon type in the equip picker (`ProfileScreen`) and the dungeon detail sheet (`CombatScreen`).

**Tech Stack:** Kotlin, kotlinx.serialization, JUnit 4, Jetpack Compose Material3, Hilt

---

## Type Assignment Reference

### Smithing weapons — type follows the bar metal

| Metal tier | Element type | Weapons |
|---|---|---|
| bronze | ground | `bronze_dagger`, `bronze_sword`, `bronze_scimitar`, `bronze_longsword`, `bronze_2h_sword`, `bronze_warhammer`, `bronze_battleaxe` |
| iron | ground | `iron_dagger`, `iron_sword`, `iron_scimitar`, `iron_longsword`, `iron_2h_sword`, `iron_warhammer`, `iron_battleaxe` |
| steel | ground | `steel_dagger`, `steel_sword`, `steel_scimitar`, `steel_longsword`, `steel_2h_sword`, `steel_warhammer`, `steel_battleaxe` |
| mithril | air | `mithril_dagger`, `mithril_sword`, `mithril_scimitar`, `mithril_longsword`, `mithril_2h_sword`, `mithril_warhammer`, `mithril_battleaxe` |
| adamantite | ground | `adamantite_dagger`, `adamantite_sword`, `adamantite_scimitar`, `adamantite_longsword`, `adamantite_2h_sword`, `adamantite_warhammer`, `adamantite_battleaxe` |
| runite | dark | `runite_dagger`, `runite_sword`, `runite_scimitar`, `runite_longsword`, `runite_2h_sword`, `runite_warhammer`, `runite_battleaxe` |

**NOT** getting `element_type`: bars, arrow tips/components, helmets, boots, chainbody, platelegs, platebody, plateskirt, pickaxes, axes.

### Fletching bows — type follows the log

| Item key | Element type | Material |
|---|---|---|
| `shortbow` | ground | log |
| `oak_shortbow` | ground | oak_log |
| `willow_shortbow` | water | willow_log |
| `maple_shortbow` | air | maple_log |
| `yew_shortbow` | light | yew_log |
| `magic_shortbow` | dark | magic_log |

### Fletching staves — type follows the rune

| Item key | Element type | Dominant rune |
|---|---|---|
| `staff_of_air` | air | air_rune |
| `staff_of_water` | water | water_rune |
| `staff_of_earth` | ground | earth_rune |
| `staff_of_fire` | fire | fire_rune |
| `staff_of_mind` | neutral | mind_rune |
| `staff_of_chaos` | dark | chaos_rune |
| `staff_of_death` | dark | death_rune |
| `staff_of_blood` | fire | blood_rune |

**NOT** getting `element_type` in fletching: `arrow_shaft` and all arrow ammunition (`bronze_arrow` through `runite_arrow`).

---

## File Map

| File | Change |
|---|---|
| `app/src/main/kotlin/com/fantasyidler/data/json/RecipeData.kt` | Add `elementType: String? = null` to `SmithingRecipe` and `FletchingRecipe` |
| `app/src/main/assets/data/recipes/smithing.json` | Add `"element_type"` to 42 weapon entries |
| `app/src/main/assets/data/recipes/fletching.json` | Add `"element_type"` to 14 bow/staff entries |
| `app/src/main/kotlin/com/fantasyidler/repository/GameDataRepository.kt` | Add `mergeWeaponTypes()` helper; update `equipment` lazy to call it |
| `app/src/test/kotlin/com/fantasyidler/repository/WeaponTypeMergeTest.kt` | New: unit tests for `mergeWeaponTypes()` |
| `app/src/main/kotlin/com/fantasyidler/ui/components/TypeChip.kt` | New: extract `TypeChip` composable to shared location |
| `app/src/main/kotlin/com/fantasyidler/ui/screen/CombatScreen.kt` | Remove local `TypeChip`; import shared one; add weapon type to `DungeonInfoSheet` |
| `app/src/main/kotlin/com/fantasyidler/ui/screen/ProfileScreen.kt` | Add `TypeChip` to `EquipPickerSheet` weapon rows |

---

## Task 1: Add `elementType` field to recipe data classes

**Files:**
- Modify: `app/src/main/kotlin/com/fantasyidler/data/json/RecipeData.kt`

- [ ] **Step 1: Add `elementType` to `SmithingRecipe` and `FletchingRecipe`**

Open `RecipeData.kt`. The current `SmithingRecipe` ends with `val timePerItem: Int`. Add `elementType` as the last field with a null default. Do the same for `FletchingRecipe`. The `CookingRecipe` and `CraftingRecipe` classes do NOT change.

Replace `SmithingRecipe` with:
```kotlin
@Serializable
data class SmithingRecipe(
    val type: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("level_required") val levelRequired: Int,
    val materials: Map<String, Int>,
    @SerialName("output_quantity") val outputQuantity: Int,
    @SerialName("xp_per_item") val xpPerItem: Double,
    @SerialName("time_per_item") val timePerItem: Int,
    @SerialName("element_type") val elementType: String? = null,
)
```

Replace `FletchingRecipe` with:
```kotlin
@Serializable
data class FletchingRecipe(
    @SerialName("item_name") val itemName: String,
    @SerialName("display_name") val displayName: String,
    val type: String,
    @SerialName("level_required") val levelRequired: Int,
    @SerialName("xp_per_item") val xpPerItem: Double,
    val materials: Map<String, Int>,
    @SerialName("output_quantity") val outputQuantity: Int,
    @SerialName("time_per_batch") val timePerBatch: Int,
    val damage: Int? = null,
    @SerialName("attack_bonus")   val attackBonus:   Int? = null,
    @SerialName("strength_bonus") val strengthBonus: Int? = null,
    val requirements: Map<String, Int> = emptyMap(),
    @SerialName("element_type") val elementType: String? = null,
)
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/kotlin/com/fantasyidler/data/json/RecipeData.kt
git commit -m "phase-3 task 1: add elementType field to SmithingRecipe and FletchingRecipe"
```

---

## Task 2: Annotate smithing weapon recipes with `element_type`

**Files:**
- Modify: `app/src/main/assets/data/recipes/smithing.json`

42 weapon entries total. Add `"element_type": "<value>"` as the last field of each weapon object (after `"time_per_item"`). Do NOT add it to bars, arrow tips, helmets, boots, chainbody, platelegs, platebody, plateskirt, pickaxes, or axes.

- [ ] **Step 1: Add `element_type` to all bronze weapons**

Find each `bronze_dagger`, `bronze_sword`, `bronze_scimitar`, `bronze_longsword`, `bronze_2h_sword`, `bronze_warhammer`, `bronze_battleaxe` and add `"element_type": "ground"` as the last field. Example:

```json
"bronze_dagger": {
  "type": "equipment",
  "display_name": "Bronze Dagger",
  "level_required": 1,
  "materials": { "bronze_bar": 1 },
  "output_quantity": 1,
  "xp_per_item": 12.5,
  "time_per_item": 60,
  "element_type": "ground"
},
```

- [ ] **Step 2: Add `element_type` to iron, steel, adamantite weapons (all `"ground"`)**

Same pattern. `iron_*`, `steel_*`, `adamantite_*` all get `"element_type": "ground"`.

- [ ] **Step 3: Add `element_type` to mithril weapons (`"air"`)**

`mithril_dagger`, `mithril_sword`, `mithril_scimitar`, `mithril_longsword`, `mithril_2h_sword`, `mithril_warhammer`, `mithril_battleaxe` → `"element_type": "air"`.

- [ ] **Step 4: Add `element_type` to runite weapons (`"dark"`)**

`runite_dagger`, `runite_sword`, `runite_scimitar`, `runite_longsword`, `runite_2h_sword`, `runite_warhammer`, `runite_battleaxe` → `"element_type": "dark"`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/assets/data/recipes/smithing.json
git commit -m "phase-3 task 2: add element_type to smithing weapon recipes"
```

---

## Task 3: Annotate fletching weapon recipes with `element_type`

**Files:**
- Modify: `app/src/main/assets/data/recipes/fletching.json`

14 entries: 6 bows + 8 staves. Add `"element_type": "<value>"` as the last field of each weapon/staff object (after `"requirements"` or whatever the current last field is). Do NOT add it to `arrow_shaft` or any arrow ammunition.

- [ ] **Step 1: Add `element_type` to bows**

```json
"shortbow": {
  ...existing fields...,
  "requirements": {},
  "element_type": "ground"
},
"oak_shortbow": {
  ...existing fields...,
  "requirements": { "ranged": 5 },
  "element_type": "ground"
},
"willow_shortbow": {
  ...existing fields...,
  "element_type": "water"
},
"maple_shortbow": {
  ...existing fields...,
  "element_type": "air"
},
"yew_shortbow": {
  ...existing fields...,
  "element_type": "light"
},
"magic_shortbow": {
  ...existing fields...,
  "element_type": "dark"
},
```

- [ ] **Step 2: Add `element_type` to staves**

```json
"staff_of_air":   { ...existing fields..., "element_type": "air"     },
"staff_of_water": { ...existing fields..., "element_type": "water"   },
"staff_of_earth": { ...existing fields..., "element_type": "ground"  },
"staff_of_fire":  { ...existing fields..., "element_type": "fire"    },
"staff_of_mind":  { ...existing fields..., "element_type": "neutral" },
"staff_of_chaos": { ...existing fields..., "element_type": "dark"    },
"staff_of_death": { ...existing fields..., "element_type": "dark"    },
"staff_of_blood": { ...existing fields..., "element_type": "fire"    },
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/assets/data/recipes/fletching.json
git commit -m "phase-3 task 3: add element_type to fletching weapon recipes"
```

---

## Task 4: Implement `mergeWeaponTypes` and update the equipment loader

**Files:**
- Modify: `app/src/main/kotlin/com/fantasyidler/repository/GameDataRepository.kt`
- Create: `app/src/test/kotlin/com/fantasyidler/repository/WeaponTypeMergeTest.kt`

### Background

`GameDataRepository.equipment` is currently a lazy property that loads `equipment.json` verbatim. We need it to also apply `elementType` from the recipe files into the resulting `EquipmentData` objects. `EquipmentData.type` is already `String? = null` (added in Phase 2 Task 3). After this task, `gameData.equipment["willow_shortbow"]?.type` will return `"water"` automatically.

- [ ] **Step 1: Write the failing test**

Create `app/src/test/kotlin/com/fantasyidler/repository/WeaponTypeMergeTest.kt`:

```kotlin
package com.fantasyidler.repository

import com.fantasyidler.data.json.EquipmentData
import com.fantasyidler.data.json.FletchingRecipe
import com.fantasyidler.data.json.SmithingRecipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeaponTypeMergeTest {

    private val baseEquipment = mapOf(
        "bronze_dagger"  to EquipmentData(name = "bronze_dagger",  displayName = "Bronze Dagger",  slot = "weapon"),
        "willow_shortbow" to EquipmentData(name = "willow_shortbow", displayName = "Willow Shortbow", slot = "weapon"),
        "wooden_bow"     to EquipmentData(name = "wooden_bow",     displayName = "Wooden Bow",     slot = "weapon"),
        "staff_of_fire"  to EquipmentData(name = "staff_of_fire",  displayName = "Staff of Fire",  slot = "weapon"),
    )

    private val smithingRecipes = mapOf(
        "bronze_dagger" to SmithingRecipe(
            type = "equipment",
            displayName = "Bronze Dagger",
            levelRequired = 1,
            materials = mapOf("bronze_bar" to 1),
            outputQuantity = 1,
            xpPerItem = 12.5,
            timePerItem = 60,
            elementType = "ground",
        ),
    )

    private val fletchingRecipes = mapOf(
        "willow_shortbow" to FletchingRecipe(
            itemName = "willow_shortbow",
            displayName = "Willow Shortbow",
            type = "weapon",
            levelRequired = 35,
            xpPerItem = 34.0,
            materials = mapOf("willow_log" to 1),
            outputQuantity = 1,
            timePerBatch = 1,
            elementType = "water",
        ),
        "staff_of_fire" to FletchingRecipe(
            itemName = "staff_of_fire",
            displayName = "Staff of Fire",
            type = "weapon",
            levelRequired = 15,
            xpPerItem = 90.0,
            materials = mapOf("maple_log" to 1, "fire_rune" to 1500),
            outputQuantity = 1,
            timePerBatch = 1,
            elementType = "fire",
        ),
    )

    @Test fun `smithing recipe element type is applied to equipment`() {
        val result = mergeWeaponTypes(baseEquipment, smithingRecipes, fletchingRecipes)
        assertEquals("ground", result["bronze_dagger"]?.type)
    }

    @Test fun `fletching recipe element type is applied to equipment`() {
        val result = mergeWeaponTypes(baseEquipment, smithingRecipes, fletchingRecipes)
        assertEquals("water", result["willow_shortbow"]?.type)
        assertEquals("fire",  result["staff_of_fire"]?.type)
    }

    @Test fun `equipment with no recipe type stays null`() {
        val result = mergeWeaponTypes(baseEquipment, smithingRecipes, fletchingRecipes)
        assertNull(result["wooden_bow"]?.type)
    }

    @Test fun `recipe key not present in equipment map is ignored`() {
        val recipesWithUnknown = smithingRecipes + mapOf(
            "ghost_sword" to SmithingRecipe(
                type = "equipment", displayName = "Ghost Sword", levelRequired = 1,
                materials = emptyMap(), outputQuantity = 1, xpPerItem = 0.0,
                timePerItem = 60, elementType = "dark",
            ),
        )
        // Should not throw; "ghost_sword" just doesn't appear in the result
        val result = mergeWeaponTypes(baseEquipment, recipesWithUnknown, fletchingRecipes)
        assertNull(result["ghost_sword"])
        assertEquals("ground", result["bronze_dagger"]?.type)
    }

    @Test fun `recipe with null element type does not overwrite existing type`() {
        val typedEquipment = mapOf(
            "bronze_dagger" to EquipmentData(name = "bronze_dagger", displayName = "Bronze Dagger",
                slot = "weapon", type = "fire"),
        )
        val recipesWithNull = mapOf(
            "bronze_dagger" to SmithingRecipe(
                type = "equipment", displayName = "Bronze Dagger", levelRequired = 1,
                materials = mapOf("bronze_bar" to 1), outputQuantity = 1, xpPerItem = 12.5,
                timePerItem = 60, elementType = null,
            ),
        )
        val result = mergeWeaponTypes(typedEquipment, recipesWithNull, emptyMap())
        // null element_type → no change; pre-existing "fire" type preserved
        assertEquals("fire", result["bronze_dagger"]?.type)
    }
}
```

- [ ] **Step 2: Run the test — confirm it fails**

```bash
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=~/Android/Sdk \
  ./gradlew testDebugUnitTest --tests "com.fantasyidler.repository.WeaponTypeMergeTest" \
  --no-daemon -Dorg.gradle.jvmargs="-Djdk.lang.Process.launchMechanism=posix_spawn" \
  2>&1 | tail -15
```

Expected: **FAIL** — `mergeWeaponTypes` is not defined yet.

- [ ] **Step 3: Implement `mergeWeaponTypes` in `GameDataRepository.kt`**

Add a package-level `internal` function at the **bottom** of `GameDataRepository.kt` (after the closing `}` of the class), before the end of the file. Then update the `equipment` lazy property.

**Add this function at the bottom of the file:**

```kotlin
/**
 * Merges [elementType] from recipe data into the static equipment map.
 *
 * Recipe entries that have a non-null [SmithingRecipe.elementType] or
 * [FletchingRecipe.elementType] overwrite the corresponding [EquipmentData.type].
 * Keys present in the recipe map but absent from [equipment] are silently ignored.
 * Null [elementType] values are also ignored (leave [EquipmentData.type] unchanged).
 *
 * Marked `internal` so it can be tested directly from the test source set.
 */
internal fun mergeWeaponTypes(
    equipment: Map<String, EquipmentData>,
    smithingRecipes: Map<String, SmithingRecipe>,
    fletchingRecipes: Map<String, FletchingRecipe>,
): Map<String, EquipmentData> {
    val types = buildMap<String, String> {
        smithingRecipes.forEach { (k, r) -> r.elementType?.let { put(k, it) } }
        fletchingRecipes.forEach { (k, r) -> r.elementType?.let { put(k, it) } }
    }
    return equipment.mapValues { (k, v) ->
        val t = types[k] ?: return@mapValues v
        v.copy(type = t)
    }
}
```

**Update the `equipment` lazy property** (currently `asset("data/equipment.json")`):

```kotlin
val equipment: Map<String, EquipmentData> by lazy {
    val base: Map<String, EquipmentData> = asset("data/equipment.json")
    mergeWeaponTypes(base, smithingRecipes, fletchingRecipes)
}
```

- [ ] **Step 4: Run the new tests — confirm they pass**

```bash
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=~/Android/Sdk \
  ./gradlew testDebugUnitTest --tests "com.fantasyidler.repository.WeaponTypeMergeTest" \
  --no-daemon -Dorg.gradle.jvmargs="-Djdk.lang.Process.launchMechanism=posix_spawn" \
  2>&1 | tail -15
```

Expected: **BUILD SUCCESSFUL**, 5 tests pass.

- [ ] **Step 5: Run all tests — confirm no regressions**

```bash
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=~/Android/Sdk \
  ./gradlew testDebugUnitTest \
  --no-daemon -Dorg.gradle.jvmargs="-Djdk.lang.Process.launchMechanism=posix_spawn" \
  2>&1 | tail -10
```

Expected: **BUILD SUCCESSFUL**, all tests pass.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/kotlin/com/fantasyidler/repository/GameDataRepository.kt \
        app/src/test/kotlin/com/fantasyidler/repository/WeaponTypeMergeTest.kt
git commit -m "phase-3 task 4: merge recipe element_type into EquipmentData at load time

mergeWeaponTypes() is a pure internal function — testable without Android
context. equipment lazy calls it after loading equipment.json, applying
element_type from smithingRecipes and fletchingRecipes into EquipmentData.type."
```

---

## Task 5: Extract TypeChip to a shared component

**Files:**
- Create: `app/src/main/kotlin/com/fantasyidler/ui/components/TypeChip.kt`
- Modify: `app/src/main/kotlin/com/fantasyidler/ui/screen/CombatScreen.kt`

Currently `TypeChip` is a `private` composable inside `CombatScreen.kt`. We need it in `ProfileScreen.kt` too. Extract it to a new shared file.

- [ ] **Step 1: Create `TypeChip.kt`**

Create `app/src/main/kotlin/com/fantasyidler/ui/components/TypeChip.kt`:

```kotlin
package com.fantasyidler.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Small pill showing an elemental type name.
 * Returns without rendering anything for null or "neutral" types.
 */
@Composable
fun TypeChip(type: String?, modifier: Modifier = Modifier) {
    if (type == null || type == "neutral") return
    val label = type.replaceFirstChar { it.uppercase() }
    Surface(
        shape    = RoundedCornerShape(4.dp),
        color    = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier,
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}
```

- [ ] **Step 2: Remove the local `TypeChip` from `CombatScreen.kt` and import the shared one**

In `CombatScreen.kt`, find and delete the `private fun TypeChip(...)` composable (it's near the top of the file, after imports). Then add the import:

```kotlin
import com.fantasyidler.ui.components.TypeChip
```

Verify the file still compiles — all existing `TypeChip(...)` call sites in `CombatScreen.kt` use the same signature so no other changes needed.

- [ ] **Step 3: Build to confirm no compile errors**

```bash
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=~/Android/Sdk \
  ./gradlew assembleDebug \
  --no-daemon -Dorg.gradle.jvmargs="-Djdk.lang.Process.launchMechanism=posix_spawn" \
  2>&1 | tail -10
```

Expected: **BUILD SUCCESSFUL**

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/com/fantasyidler/ui/components/TypeChip.kt \
        app/src/main/kotlin/com/fantasyidler/ui/screen/CombatScreen.kt
git commit -m "phase-3 task 5: extract TypeChip to shared ui/components package"
```

---

## Task 6: Surface weapon type in the equip picker and dungeon detail sheet

**Files:**
- Modify: `app/src/main/kotlin/com/fantasyidler/ui/screen/ProfileScreen.kt`
- Modify: `app/src/main/kotlin/com/fantasyidler/ui/screen/CombatScreen.kt`

### Part A — ProfileScreen `EquipPickerSheet`

The `EquipPickerSheet` composable lists equipment candidates in a `LazyColumn`. Each row shows a `Column` with the item's `displayName` and a `detail` string. We want to show a `TypeChip` for weapon-slot items that have a non-null type.

- [ ] **Step 1: Add `TypeChip` import and show it in `EquipPickerSheet`**

Add import at the top of `ProfileScreen.kt`:
```kotlin
import com.fantasyidler.ui.components.TypeChip
```

In `EquipPickerSheet`, find the `items(candidates.sortedWith(...))` block. Each `item` renders a `Row` with a `Column` inside. After the `Text(displayName)` line (and after the optional `detail` Text), add a `TypeChip` for weapon-slot items:

```kotlin
items(
    candidates.sortedWith(
        compareBy({ it.requirements.values.maxOrNull() ?: 0 }, { it.name })
    )
) { item ->
    val xpLabel = weaponXpLabel(item.combatStyle, context).takeIf { item.slot == EquipSlot.WEAPON }
    val displayName = buildString {
        append(GameStrings.itemName(context, item.name))
        if (xpLabel != null) append(" ($xpLabel)")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEquip(item.name) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                displayName,
                style = MaterialTheme.typography.bodyLarge,
            )
            // Show type chip for typed weapons
            if (item.slot == EquipSlot.WEAPON) {
                TypeChip(item.type, modifier = Modifier.padding(top = 2.dp))
            }
            val detail = buildEquipDetail(item, context)
            if (detail.isNotEmpty()) {
                Text(
                    text  = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text  = stringResource(R.string.btn_equip),
            style = MaterialTheme.typography.labelMedium,
            color = GoldPrimary,
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}
```

### Part B — CombatScreen `DungeonInfoSheet`

The `DungeonInfoSheet` already shows `combatStyle` via `StatRow`. Add weapon type below it using `TypeChip`.

- [ ] **Step 2: Add weapon type chip to `DungeonInfoSheet`**

In `DungeonInfoSheet`, find this line:
```kotlin
StatRow(label = stringResource(R.string.label_combat_style), value = styleLabel, valueColor = GoldPrimary)
```

Add immediately after it:
```kotlin
equippedWeapon?.type?.takeIf { it != "neutral" }?.let { weaponType ->
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp),
    ) {
        Text(
            text  = stringResource(R.string.label_weapon_type),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TypeChip(weaponType)
    }
}
```

Then add the string resource. Open `app/src/main/res/values/strings.xml` and add:
```xml
<string name="label_weapon_type">Weapon type</string>
```

**Important:** Check the existing `strings.xml` first to confirm `label_combat_style` is defined there (it's referenced in step 2). The new entry follows the same pattern.

- [ ] **Step 3: Build to confirm no compile errors**

```bash
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=~/Android/Sdk \
  ./gradlew assembleDebug \
  --no-daemon -Dorg.gradle.jvmargs="-Djdk.lang.Process.launchMechanism=posix_spawn" \
  2>&1 | tail -10
```

Expected: **BUILD SUCCESSFUL**

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/com/fantasyidler/ui/screen/ProfileScreen.kt \
        app/src/main/kotlin/com/fantasyidler/ui/screen/CombatScreen.kt \
        app/src/main/res/values/strings.xml
git commit -m "phase-3 task 6: show weapon type in equip picker and dungeon detail sheet

TypeChip appears below weapon name in ProfileScreen's EquipPickerSheet.
TypeChip appears next to combat style label in CombatScreen's DungeonInfoSheet."
```

---

## Task 7: Final verification

- [ ] **Step 1: Run full test suite**

```bash
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=~/Android/Sdk \
  ./gradlew testDebugUnitTest \
  --no-daemon -Dorg.gradle.jvmargs="-Djdk.lang.Process.launchMechanism=posix_spawn" \
  2>&1 | tail -15
```

Expected: **BUILD SUCCESSFUL**, all tests pass (TypeRegistryTest + CombatSimulatorTypeTest + WeaponTypeMergeTest).

- [ ] **Step 2: Build debug APK**

```bash
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=~/Android/Sdk \
  ./gradlew assembleDebug \
  --no-daemon -Dorg.gradle.jvmargs="-Djdk.lang.Process.launchMechanism=posix_spawn" \
  2>&1 | tail -10
```

Expected: **BUILD SUCCESSFUL**

- [ ] **Step 3: Smoke-check the type data is wired end-to-end**

This can't be a unit test (needs Android context), but confirm visually:
- In `GameDataRepository`, `equipment["willow_shortbow"]?.type` should resolve to `"water"` after merge
- The combat type multiplier for a willow shortbow vs a fire enemy should be 0.67× (water is weak vs fire: fire is 1 step CW from water? Let me check: wheel = `[fire(0), dark(1), ice(2), light(3), ground(4), lightning(5), air(6), water(7)]`. Attacker=water(7) vs defender=fire(0): cw = (0-7+8)%8 = 1 → **strong**! Actually water beats fire. And fire beats water would be: attacker=fire(0) vs defender=water(7): cw = (7-0+8)%8 = 7 = size-1 → **weak**. So equipping a water bow vs fire enemies gives 1.5×. This matches game intuition: water beats fire.)

A quick sanity check you can add as a comment in `WeaponTypeMergeTest.kt` if desired:
```
// Type wheel: fire→dark→ice→light→ground→lightning→air→water
// willow_shortbow = water type
// water(7) vs fire(0): cw = (0-7+8)%8 = 1 → STRONG (1.5×) ✓  water beats fire
// fire(0) vs water(7): cw = (7-0+8)%8 = 7 = size-1 → WEAK (0.67×) ✓  fire loses to water
```

- [ ] **Step 4: Push to remote**

```bash
git push -u origin phase-3-weapon-types
```
