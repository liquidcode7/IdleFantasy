# Phase 1 — Foundation: Types

**Goal:** Every material has an elemental type. The type effectiveness wheel exists
in code and can be queried. No gameplay changes yet.

**Estimated effort:** A few short Claude Code sessions. Mostly JSON + small Kotlin.

**Definition of done:**
- All material JSONs have a `type` field
- `type_effectiveness.json` exists with the 8-type wheel
- `TypeRegistry.kt` loads it and provides query functions
- Existing tests pass, app builds and runs identically to before

---

## Step 1 — Decide Type Assignments

Before touching code, decide what type each existing material is. This is a
creative call. Suggested starting point below — adjust to taste.

### Ores (`assets/data/ores.json`)

| Material | Suggested Type | Reasoning |
|------------------|----------------|---------------------------|
| rune_essence | (special) | Used for spell catalysts |
| copper_ore | Ground | Basic earth metal |
| tin_ore | Ground | Basic earth metal |
| iron_ore | Ground | Earthen, common |
| silver_ore | Light | Traditionally holy/light |
| coal | Fire | Burns, fire association |
| gold_ore | Light | Sun/light association |
| mithril_ore | Air | Light, ethereal metal |
| adamantite_ore | Ground | Sturdy earth |
| runite_ore | Dark | Mystical, deep arcane |

### Logs (from `trees.json` — assign to the log output, not the tree)

| Material | Suggested Type |
|--------------|----------------|
| logs (normal) | Ground |
| oak_logs | Ground |
| willow_logs | Water |
| maple_logs | Air |
| yew_logs | Light |
| magic_logs | Dark |
| redwood_logs | Fire |

### Fish (`fish.json`)

All fish are Water type unless you want to get fancy. Could split deep-sea
species like sharks/manta_ray/sea_turtle as a stronger Water and keep shallow
ones as Water too — keep it simple to start.

### Runes (`runes.json`)

Runes are already thematically typed — easy mapping:

| Material | Type |
|--------------|----------|
| air_rune | Air |
| water_rune | Water |
| earth_rune | Ground |
| fire_rune | Fire |
| mind_rune | (special — catalyst) |
| chaos_rune | Dark |
| death_rune | Dark |
| blood_rune | Dark |

No Lightning, Ice, or Light runes exist yet. Don't add them in Phase 1 —
just type what's there.

### Gems

Open question — could go thematic (sapphire=Water, ruby=Fire, emerald=Nature
but we removed Nature so maybe Ground, diamond=Light, dragonstone=Dark).
Your call.

**Important:** Mark special items like rune_essence and mind_rune with type
`neutral` or `null` rather than forcing them into the wheel. Not everything
needs a type.

---

## Step 2 — Update Material JSONs

For each material file, add a `type` field. Example for `ores.json`:

```json
"iron_ore": {
  "display_name": "Iron Ore",
  "level_required": 15,
  "xp_per_ore": 35,
  "time_per_ore": 1,
  "type": "ground"
}
```

Files to update:
- `app/src/main/assets/data/ores.json`
- `app/src/main/assets/data/trees.json` (add type to the log_name reference, or to a new field on the tree entry)
- `app/src/main/assets/data/fish.json`
- `app/src/main/assets/data/runes.json`
- `app/src/main/assets/data/gems.json`

---

## Step 3 — Create `type_effectiveness.json`

Path: `app/src/main/assets/data/type_effectiveness.json`

```json
{
  "types": ["fire", "air", "ground", "lightning", "water", "ice", "dark", "light"],
  "wheel_order": ["fire", "air", "ground", "lightning", "water", "ice", "dark", "light"],
  "strong_multiplier": 1.5,
  "weak_multiplier": 0.67,
  "neutral_multiplier": 1.0
}
```

The wheel is positional — each type beats the next 2 clockwise, loses to the
previous 2. `TypeRegistry` computes this from `wheel_order` rather than
hardcoding every pair.

---

## Step 4 — Add Data Classes

Update the deserialization classes in `app/src/main/kotlin/com/fantasyidler/data/json/`:

- Add `val type: String? = null` to `OreData`, `FishData`, `TreeData` (or `LogData`), `GemData`, etc.
- Default to null so existing materials without the field don't break

Create a new file `app/src/main/kotlin/com/fantasyidler/data/json/TypeEffectivenessData.kt`:

```kotlin
@Serializable
data class TypeEffectivenessData(
    val types: List<String>,
    @SerialName("wheel_order") val wheelOrder: List<String>,
    @SerialName("strong_multiplier") val strongMultiplier: Float,
    @SerialName("weak_multiplier") val weakMultiplier: Float,
    @SerialName("neutral_multiplier") val neutralMultiplier: Float,
)
```

---

## Step 5 — Build `TypeRegistry`

Path: `app/src/main/kotlin/com/fantasyidler/simulator/TypeRegistry.kt`

API:
```kotlin
object TypeRegistry {
    fun init(data: TypeEffectivenessData)
    fun multiplier(attacker: String?, defender: String?): Float
    fun isValidType(type: String): Boolean
}
```

Loading logic: same pattern as how other JSON data is loaded — look at
`GameDataRepository` for the existing pattern. Load `type_effectiveness.json`
on app start and call `TypeRegistry.init(...)`.

Multiplier logic:
- If either type is null or "neutral", return 1.0
- Find positions in wheel
- Distance 1 or 2 clockwise = strong (1.5×)
- Distance 1 or 2 counter-clockwise = weak (0.67×)
- Otherwise = neutral (1.0×)

---

## Step 6 — Verify

- `./gradlew build` passes
- Open the app on your device — it should play identically
- Add a tiny unit test for `TypeRegistry` checking a few multiplier cases
  (Fire vs Air = 1.5, Fire vs Water = 0.67, Fire vs Light = 1.0)

---

## Suggested Claude Code Prompt to Kick Off

> Read `docs/VISION.md`, `docs/ROADMAP.md`, and `docs/phase-1-types.md`. We're
> starting Phase 1. Begin by reading `IMPLEMENTATION_PLAN.md` and the existing
> JSON files in `app/src/main/assets/data/` to understand the current
> structure. Then walk me through your plan for Step 2 before making changes.

Make Claude Code explain its plan **before** it writes code. You'll learn the
codebase faster and catch misunderstandings early.

---

## When You're Done

- Commit each step separately (easy to revert if something breaks)
- Open a PR from `phase-1-types` branch to your `main`
- Merge when green
- Take a day off, then start Phase 2
