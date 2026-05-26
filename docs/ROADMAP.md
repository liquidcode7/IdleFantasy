# Roadmap

Five phases, each shippable on its own. Ship a working build after every phase.
Play it for a few days before starting the next.

---

## Phase 1 — Foundation: Types

**Goal:** Every material has a type. The type effectiveness wheel exists in code.

- Add `type` field to materials in `ores.json`, `trees.json`, `fish.json`, `runes.json`, `gems.json`
- Create `assets/data/type_effectiveness.json` defining the 8-type wheel
- Build `TypeRegistry.kt` — loads the wheel, exposes `multiplier(attacker, defender)`
- Add types to `data/json/*Data.kt` deserialization classes

**No gameplay changes yet.** This is pure scaffolding. The build should pass
and the game should play identically.

**Why first:** Everything else depends on this.

---

## Phase 2 — Combat Means Something

**Goal:** Type effectiveness applies in dungeons. Players feel it.

- Add `type` to enemies in `enemies.json`
- Add `primary_type` to dungeons in `dungeons/*.json`
- Modify `CombatSimulator` to apply effectiveness multipliers to damage
- Surface type information in the combat/dungeon UI so players can see what they're fighting

**After this phase:** Choosing the right gear for a dungeon already matters.

---

## Phase 3 — Equipment Inherits Type

**Goal:** Crafted gear takes its type from its primary material.

- Add `materials_used` field to recipes (the inputs that determine type)
- Add `primary_material` concept — the type-defining input for an item
- Compute equipment type at runtime from the recipe that made it
- Update inventory and equip screens to show item type

**This is the hardest core phase.** Take your time. The combat type system from
Phase 2 already works without this — gear just doesn't have type yet.

---

## Phase 4 — Regions and Travel

**Goal:** The world has structure. You can't get everything everywhere.

- Create `regions.json` — 8 regions, each with type, unlock requirements, available materials
- Add region-locked availability to materials
- Build `TravelSession` reusing existing WorkManager infrastructure
- Add a "current region" concept to player state
- Region selection UI

**After this phase:** The full core loop is real. Gather typed materials in
regions, craft typed gear, beat typed dungeons, unlock deeper regions.

---

## Phase 5 — Crafting Depth

**Goal:** Crafting has identity and progression.

Three sub-phases, do them in order:

**5a. Quality tiers** — Standard / Good / Fine / Masterwork / Legendary.
Higher recipe mastery raises the odds of higher tiers.

**5b. Recipe mastery** — Track craft count per recipe in a new Room entity.
Tiers: Apprentice (1+) / Journeyman (25+) / Expert (100+) / Master (500+).

**5c. Recipe discovery** — Recipes hidden until discovered through material
combination. New persistence layer for "discovered recipes." This is the
hardest single piece in the whole project.

**After this phase:** The vision is real. Ship 1.0.

---

## Working Rules

- One phase = one branch. Merge to main when shippable.
- Periodically sync upstream into main, then rebase your phase branch.
- After every phase: install on real device, play for a few days, take notes
  before starting the next.
- If a phase grows beyond 2-3 weeks of work, split it.
- Document what you actually did in `docs/changelog/phase-N.md` as you go.

## Out of Scope (For Now)

- Multiplayer / trading / leaderboards
- Cloud saves
- New skills beyond what exists
- Major UI redesign

These can come later. Stay focused on the type/region/crafting trio.
