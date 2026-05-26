package com.fantasyidler.simulator

import com.fantasyidler.data.json.TypeEffectivenessData

/**
 * Provides elemental type effectiveness multipliers derived from the 8-type wheel.
 *
 * Initialise once on app start by calling [init] with the loaded
 * [TypeEffectivenessData].  After that, call [multiplier] anywhere to get the
 * damage modifier for an attacker/defender type pair.
 *
 * Wheel (clockwise): Fire → Dark → Ice → Light → Ground → Lightning → Air → Water
 *   - 1 or 2 steps clockwise  = strong  (1.5× by default)
 *   - 1 or 2 steps counter-clockwise = weak (0.67× by default)
 *   - all other distances      = neutral (1.0×)
 *   - null or "neutral" on either side → neutral (1.0×)
 */
object TypeRegistry {

    private const val NEUTRAL = "neutral"

    private var wheelOrder: List<String> = emptyList()
    private var strongMultiplier: Float = 1.5f
    private var weakMultiplier: Float = 0.67f
    private var neutralMultiplier: Float = 1.0f

    /** Call once on app start before any [multiplier] queries. */
    fun init(data: TypeEffectivenessData) {
        wheelOrder = data.wheelOrder
        strongMultiplier = data.strongMultiplier
        weakMultiplier = data.weakMultiplier
        neutralMultiplier = data.neutralMultiplier
    }

    /**
     * Returns the damage multiplier when [attacker] hits [defender].
     *
     * Either argument being null or "neutral" returns 1.0.
     * Unrecognised type strings also return 1.0 (safe default).
     */
    fun multiplier(attacker: String?, defender: String?): Float {
        if (attacker == null || defender == null) return neutralMultiplier
        if (attacker == NEUTRAL || defender == NEUTRAL) return neutralMultiplier
        if (attacker == defender) return neutralMultiplier

        val aIdx = wheelOrder.indexOf(attacker)
        val dIdx = wheelOrder.indexOf(defender)
        if (aIdx == -1 || dIdx == -1) return neutralMultiplier

        val size = wheelOrder.size
        // Clockwise steps from attacker to defender
        val cw = (dIdx - aIdx + size) % size

        return when (cw) {
            1, 2 -> strongMultiplier             // attacker beats defender
            size - 1, size - 2 -> weakMultiplier // attacker loses to defender
            else -> neutralMultiplier
        }
    }

    /** Returns true if [type] is a known wheel type or the special "neutral" value. */
    fun isValidType(type: String): Boolean =
        type == NEUTRAL || wheelOrder.contains(type)
}
