package com.tokensmp.token;

/**
 * Every active ability implemented by the ability engine.
 *
 * This is the token roster supplied by the server owner: one signature ability
 * per token, present in all three tiers (tier-wise scaled). No two tokens
 * share an ability. The three admin abilities stay isolated with the Admin
 * Token.
 */
public enum TokenAbility {

    /** No active ability configured for this token yet. */
    NONE,

    // ---- 1. Zombie ----
    UNDEAD_VITALITY,
    // ---- 2. Skeleton ----
    ARCHERS_FOCUS,
    // ---- 3. Spider ----
    WEB_TRAPPER,
    // ---- 4. Creeper ----
    MEGA_DETONATOR,
    // ---- 5. Blaze ----
    INFERNAL_AURA,
    // ---- 6. Enderman ----
    VOID_SHIFT,
    // ---- 7. Warden ----
    SONIC_SHRIEK,
    // ---- 8. Ender Dragon ----
    DRAGONS_MIGHT,
    // ---- 9. Guardian ----
    OCEANS_GRACE,
    // ---- 10. Wither Skeleton ----
    DARK_PRESENCE,
    // ---- 11. Ravager ----
    BERSERKER_RAGE,
    // ---- 12. Wolf ----
    PACK_LEADER,
    // ---- 13. Pig ----
    HOG_HEAL,
    // ---- 14. Chicken ----
    FEATHER_FALL,
    // ---- 15. Snow Golem ----
    FROZEN_HEART,
    // ---- 16. Bee ----
    BUZZ_RUSH,
    // ---- 17. Horse ----
    STALLION_SPRINT,
    // ---- 18. Bat ----
    ECHOLOCATION,
    // ---- 19. Squid ----
    INK_CLOUD,
    // ---- 20. Witch ----
    BREW_MASTER,

    // ------------------------------------------------------------------
    // Admin Token (isolated - never part of the player ability set)
    // ------------------------------------------------------------------
    NETHER_SHOCKWAVE,
    CHRONO_FREEZE,
    SERVER_JUDGMENT
}
