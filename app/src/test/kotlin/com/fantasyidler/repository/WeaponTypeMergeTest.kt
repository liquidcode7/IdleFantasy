package com.fantasyidler.repository

import com.fantasyidler.data.json.EquipmentData
import com.fantasyidler.data.json.FletchingRecipe
import com.fantasyidler.data.json.SmithingRecipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeaponTypeMergeTest {

    private val baseEquipment = mapOf(
        "bronze_dagger"   to EquipmentData(name = "bronze_dagger",   displayName = "Bronze Dagger",   slot = "weapon"),
        "willow_shortbow" to EquipmentData(name = "willow_shortbow", displayName = "Willow Shortbow", slot = "weapon"),
        "wooden_bow"      to EquipmentData(name = "wooden_bow",      displayName = "Wooden Bow",      slot = "weapon"),
        "staff_of_fire"   to EquipmentData(name = "staff_of_fire",   displayName = "Staff of Fire",   slot = "weapon"),
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
        assertEquals("fire", result["bronze_dagger"]?.type)
    }
}
