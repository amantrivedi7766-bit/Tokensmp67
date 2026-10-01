package com.tokensmp.token;

/**
 * Every distinct active ability implemented by the ability engine.
 *
 * The 45 player abilities (15 tokens x 3 tiers) each map to exactly one of
 * these values - no two tokens share an implementation, every tier of every
 * token has a different mechanic, and every ability deals real server-side
 * damage. The three admin abilities are isolated with the Admin Token.
 */
public enum TokenAbility {

    /** No active ability. */
    NONE,

    // ------------------------------------------------------------------
    // 1. Enderman
    // ------------------------------------------------------------------
    VOID_PIERCE,
    VOID_RIFT,
    ENDER_COLLAPSE,

    // ------------------------------------------------------------------
    // 2. Creeper
    // ------------------------------------------------------------------
    BLAST_FIST,
    VOLATILE_CHARGE,
    CATACLYSM_DETONATION,

    // ------------------------------------------------------------------
    // 3. Skeleton
    // ------------------------------------------------------------------
    BONE_BOLT,
    RICOCHET_SHOT,
    DEADEYE_BARRAGE,

    // ------------------------------------------------------------------
    // 4. Ghast
    // ------------------------------------------------------------------
    GHAST_ORB,
    INFERNO_COMET,
    NETHERFALL,

    // ------------------------------------------------------------------
    // 5. Warden
    // ------------------------------------------------------------------
    SONIC_JAB,
    SONIC_BEAM,
    SONIC_RUPTURE,

    // ------------------------------------------------------------------
    // 6. Piglin
    // ------------------------------------------------------------------
    GOLDEN_CLEAVE,
    GOLD_SPEAR,
    ROYAL_EXECUTION,

    // ------------------------------------------------------------------
    // 7. Fish
    // ------------------------------------------------------------------
    AQUA_BULLET,
    TIDAL_RAM,
    LEVIATHAN_CRASH,

    // ------------------------------------------------------------------
    // 8. Zombie
    // ------------------------------------------------------------------
    ROTTEN_SMASH,
    GRAVE_BREAKER,
    UNDEAD_CATACLYSM,

    // ------------------------------------------------------------------
    // 9. Wither
    // ------------------------------------------------------------------
    WITHER_SKULL,
    TRIPLE_SKULL_VOLLEY,
    WITHER_BARRAGE,

    // ------------------------------------------------------------------
    // 10. Villager
    // ------------------------------------------------------------------
    EMERALD_LANCE,
    TRADE_BREAKER,
    EMERALD_JUDGMENT,

    // ------------------------------------------------------------------
    // 11. Slime
    // ------------------------------------------------------------------
    SLIME_SLAM,
    BOUNCY_CRUSH,
    MEGA_SLIME_IMPACT,

    // ------------------------------------------------------------------
    // 12. Magma Cube
    // ------------------------------------------------------------------
    MAGMA_SLAM,
    MAGMA_WAVE,
    MAGMA_CORE_ERUPTION,

    // ------------------------------------------------------------------
    // 13. Illusioner
    // ------------------------------------------------------------------
    PHANTOM_ARROW,
    MIRROR_VOLLEY,
    REALITY_FRACTURE,

    // ------------------------------------------------------------------
    // 14. Blaze
    // ------------------------------------------------------------------
    FLAME_LANCE,
    INFERNAL_SPIRAL,
    SOLAR_BURST,

    // ------------------------------------------------------------------
    // 15. Golem
    // ------------------------------------------------------------------
    IRON_FIST,
    IRONQUAKE,
    COLOSSUS_IMPACT,

    // ------------------------------------------------------------------
    // Admin Token (isolated - never part of the player ability set)
    // ------------------------------------------------------------------
    NETHER_SHOCKWAVE,
    CHRONO_FREEZE,
    SERVER_JUDGMENT
}
