package com.tokensmp.token;

/**
 * The distinct ability behaviors implemented by the ability package. Every
 * token's ability maps to exactly one of these - implementations are never
 * duplicated between tokens.
 */
public enum TokenAbility {

    /** No active ability (pure passive tier). */
    NONE,

    /** Zombie T3: temporary absorption hearts + trail (Undead Enrage). */
    UNDEAD_ENRAGE,

    /** Creeper T3: charged explosion nova with real AoE damage + knockback. */
    CHARGED_OVERLOAD,

    /** Blaze T3: fire nova igniting every enemy in radius. */
    BLAZING_WRATH,

    /** Enderman T3: forward teleport strike with true damage on landing. */
    WARP_STRIKE,

    /** Warden T3: forward sonic ray, true damage + heavy knockback. */
    SONIC_BOOM,

    /** Admin T1: nether shockwave around the user (Nether Overlord). */
    NETHER_SHOCKWAVE,

    /** Admin T2: freezes every nearby player in time (Ruler of the Realm). */
    CHRONO_FREEZE,

    /** Admin T3: god-tier forward judgment projectile (God Mode). */
    SERVER_JUDGMENT
}
