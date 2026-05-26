# Sigil Enchanting System — Design Spec

**Date:** 2026-05-26
**Phase:** Post Phase 4 (depends on regions, typed dungeons, typed gear)
**Status:** Approved, awaiting implementation plan

---

## Overview

Sigils are a new category of craftable item that slot into weapons, armor, and tools
to provide stat bonuses. They are crafted via the Runecrafting skill using a combination
of region-gathered materials, dungeon boss drops, and existing runes. Gear must be
unlocked for enchanting via one-time Artificer dungeons before sigils can be slotted.

Inspired by Monster Hunter's decoration system (slot limits, item rarity determines
slot count) and Dark Souls' raw infusion (elemental vs raw as a meaningful build choice,
not a fallback).

---

## Dependencies

This phase cannot be implemented before:
- **Phase 2** — typed dungeons and dungeon boss drops
- **Phase 3** — typed gear (sigil synergy depends on gear type)
- **Phase 4** — regions (elemental sigil ingredients are region-locked)

---

## Sigil Tiers

Three tiers. Tier determines power and crafting cost, not slot compatibility — a slot
is a slot; any tier fits any slot.

| Tier | Runecrafting level required | Rune essence to slot |
|---|---|---|
| Minor | 50 | 10 |
| Major | 65 | 25 |
| Greater | 80 | 50 |

All stat values are placeholder. Tune during Phase 5 playtesting.

---

## Elemental vs Raw

### Elemental Sigils

- Typed to one of the 8 elements (fire, dark, ice, light, ground, lightning, air, water)
- Provide a **base bonus** that always applies
- Provide a **synergy bonus** when the sigil's type matches the host item's type
  (e.g. a fire sigil slotted into a fire sword gives base + synergy)
- Synergy stacks across multiple matching sigils in the same item
- Interact with the type effectiveness wheel — fire damage bonuses apply the
  wheel multipliers when fighting typed enemies

### Raw Sigils

- No elemental type
- **Higher flat stat numbers** than an elemental sigil of the same tier with no
  active synergy — raw is a real build choice, not a fallback
- **Consistent** — unaffected by enemy type matchups in either direction. Raw
  damage never gets the 1.5× strong bonus but never eats the 0.67× weak penalty
- Trade-off: no synergy bonus available; you pay more runes at crafting time to
  compensate for not needing region materials

---

## Stat Categories

| Category | Examples |
|---|---|
| Combat | +flat damage, +defense, +max HP |
| Skill | +XP gain %, +gathering speed |
| Type combat | +damage vs specific enemy type, +resistance to a damage type |
| Special (Greater only) | chance-based effects, dual-stat bonuses |

Specific values to be determined during playtesting.

---

## Sigil Slot System

### Slot count by item rarity

Low-level / common gear has no slots. Slots appear on gear once it reaches
meaningful rarity thresholds:

| Item rarity | Sigil slots |
|---|---|
| Common (low level) | 0 |
| Uncommon | 1 |
| Rare | 2 |
| Legendary / unique | 3 |

No slot types — a slot is a slot. Rarity of the item is the differentiator.

> **Implementation note:** Phase 6 (Crafting Depth) is where full quality tiers
> (Standard → Good → Fine → Masterwork → Legendary) are built. Until then, slot
> count can be proxied by item level requirement thresholds — e.g. level 1–39 = 0
> slots, 40–59 = 1 slot, 60–79 = 2 slots, 80+ = 3 slots. Retrofit to true rarity
> tiers in Phase 6.

### Item categories

Sigil slotting applies to three item categories:
- **Weapons**
- **Armor** (per piece — chest/legs get more slots than helm/boots per rarity tier)
- **Tools** (gathering and crafting tools)

### Slotting mechanics

- **Cost to slot:** Rune essence (see tier table above)
- **Removal:** Free to initiate, but the sigil is **destroyed on removal**. Rune
  essence is not refunded. Confirms before executing with a clear destruction warning.
- No accidental slots — all slot/unslot actions go through a confirmation screen
  showing cost and consequences

---

## Crafting Recipe Structure

Sigil crafting takes place at the runecrafting bench. Every sigil requires all three
ingredient types simultaneously:

### 1. Region material
A new gatherable ingredient found only in the matching elemental region (Phase 4).
- Fire sigil → Caldera ingredient
- Ice sigil → Frostveil ingredient
- (etc. — one new ingredient per region)
- **Raw sigils use rune essence instead** — no region lock, available from the start

### 2. Dungeon boss drop
A rare catalyst dropped by dungeon bosses. Not type-specific — any boss drop works.
Drop rate is low enough to require consistent dungeon running. This ingredient is
shared across all sigil types and tiers (quantity scales with tier).

### 3. Existing runes
Consumed as magical fuel:
- Elemental sigils consume their matching rune (fire sigil → fire_runes)
- Raw sigils consume mind_runes (reinforces mind_rune's neutral catalyst identity)
- Higher tiers consume more runes

### Raw sigil crafting cost
Raw sigils consume **more runes** than their elemental equivalent at the same tier.
This compensates for not requiring a region material — you're paying in runes for
the consistency and higher flat stats.

---

## Unlock Flow

Slotting sigils into any item category is locked until the player completes the
corresponding one-time Artificer dungeon. Sigils can be owned before unlocking;
they just can't be slotted.

| Unlock | Dungeon | Suggested gate |
|---|---|---|
| Weapon enchanting | Artificer's Forge | Combat level 40 |
| Armor enchanting | Artificer's Sanctum | Combat level 55 |
| Tool enchanting | Artificer's Workshop | Any gathering skill level 50 |

- Unlocks are permanent and account-wide
- Can be completed in any order
- The Artificer mini-boss drops a one-time unlock token consumed on use
- Token does not persist in inventory after use
- Gate levels are placeholder — tune during playtesting

---

## Phase Placement

This system is a standalone phase after Phase 4. Suggested label: **Phase 5 — Enchanting**,
pushing the existing Phase 5 (Crafting Depth) to Phase 6.

Alternatively, Enchanting could become a Phase 5d sub-phase slotted after 5a/5b/5c if
the crafting depth work is tackled first. Decision deferred — revisit after Phase 4
playtesting.

---

## Open Questions (deferred to playtesting)

- Exact stat values for all sigil tiers and types
- Exact rune costs per tier (elemental and raw)
- Boss drop rates for the catalyst ingredient
- Level gates for Artificer dungeons
- Whether armor slot counts should vary by slot (chest > helm) or be flat per rarity
- Whether tools should have a separate rarity track or share the weapon/armor rarity tiers
