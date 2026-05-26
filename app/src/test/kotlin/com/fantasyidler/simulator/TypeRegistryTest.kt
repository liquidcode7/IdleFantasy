package com.fantasyidler.simulator

import com.fantasyidler.data.json.TypeEffectivenessData
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [TypeRegistry].
 *
 * Wheel under test: Fire → Dark → Ice → Light → Ground → Lightning → Air → Water
 */
class TypeRegistryTest {

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

    // --- Strong matchups (1 step clockwise) ---

    @Test fun `fire beats dark`() =
        assertEquals(1.5f, TypeRegistry.multiplier("fire", "dark"), 0.001f)

    @Test fun `water beats fire`() =
        assertEquals(1.5f, TypeRegistry.multiplier("water", "fire"), 0.001f)

    @Test fun `ground beats lightning`() =
        assertEquals(1.5f, TypeRegistry.multiplier("ground", "lightning"), 0.001f)

    @Test fun `dark beats light`() =
        assertEquals(1.5f, TypeRegistry.multiplier("dark", "light"), 0.001f)

    // --- Strong matchups (2 steps clockwise) ---

    @Test fun `fire beats ice`() =
        assertEquals(1.5f, TypeRegistry.multiplier("fire", "ice"), 0.001f)

    @Test fun `water beats dark`() =
        assertEquals(1.5f, TypeRegistry.multiplier("water", "dark"), 0.001f)

    @Test fun `lightning beats water`() =
        assertEquals(1.5f, TypeRegistry.multiplier("lightning", "water"), 0.001f)

    // --- Weak matchups ---

    @Test fun `fire loses to water`() =
        assertEquals(0.67f, TypeRegistry.multiplier("fire", "water"), 0.001f)

    @Test fun `fire loses to air`() =
        assertEquals(0.67f, TypeRegistry.multiplier("fire", "air"), 0.001f)

    @Test fun `lightning loses to ground`() =
        assertEquals(0.67f, TypeRegistry.multiplier("lightning", "ground"), 0.001f)

    @Test fun `light loses to dark`() =
        assertEquals(0.67f, TypeRegistry.multiplier("light", "dark"), 0.001f)

    // --- Neutral matchups ---

    @Test fun `fire vs ground is neutral`() =
        assertEquals(1.0f, TypeRegistry.multiplier("fire", "ground"), 0.001f)

    @Test fun `fire vs lightning is neutral`() =
        assertEquals(1.0f, TypeRegistry.multiplier("fire", "lightning"), 0.001f)

    // --- Special cases ---

    @Test fun `same type is neutral`() =
        assertEquals(1.0f, TypeRegistry.multiplier("fire", "fire"), 0.001f)

    @Test fun `null attacker is neutral`() =
        assertEquals(1.0f, TypeRegistry.multiplier(null, "fire"), 0.001f)

    @Test fun `null defender is neutral`() =
        assertEquals(1.0f, TypeRegistry.multiplier("fire", null), 0.001f)

    @Test fun `neutral attacker is neutral`() =
        assertEquals(1.0f, TypeRegistry.multiplier("neutral", "fire"), 0.001f)

    @Test fun `neutral defender is neutral`() =
        assertEquals(1.0f, TypeRegistry.multiplier("fire", "neutral"), 0.001f)

    @Test fun `unknown type is neutral`() =
        assertEquals(1.0f, TypeRegistry.multiplier("fire", "cheese"), 0.001f)

    // --- Attacker coverage for ice and air (previously untested as attackers) ---

    @Test fun `ice beats light`() =
        assertEquals(1.5f, TypeRegistry.multiplier("ice", "light"), 0.001f)

    @Test fun `ice beats ground`() =
        assertEquals(1.5f, TypeRegistry.multiplier("ice", "ground"), 0.001f)

    @Test fun `air beats water`() =
        assertEquals(1.5f, TypeRegistry.multiplier("air", "water"), 0.001f)

    @Test fun `air beats fire`() =
        assertEquals(1.5f, TypeRegistry.multiplier("air", "fire"), 0.001f)

    @Test fun `ice loses to fire`() =
        assertEquals(0.67f, TypeRegistry.multiplier("ice", "fire"), 0.001f)

    @Test fun `air loses to lightning`() =
        assertEquals(0.67f, TypeRegistry.multiplier("air", "lightning"), 0.001f)

    // --- isValidType ---

    @Test fun `fire is a valid type`() =
        assertEquals(true, TypeRegistry.isValidType("fire"))

    @Test fun `neutral is a valid type`() =
        assertEquals(true, TypeRegistry.isValidType("neutral"))

    @Test fun `unknown string is not valid`() =
        assertEquals(false, TypeRegistry.isValidType("cheese"))
}
