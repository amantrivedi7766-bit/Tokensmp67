package com.tokensmp.token;

/**
 * Every active ability implemented by the ability engine.
 *
 * One signature ability per token, present in all three tiers (tier-wise
 * scaled). No two tokens share an ability. The reserved Owner/Admin tokens
 * stay isolated from the player-facing roster.
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

    // ---- 21. Founder / Owner ----
    WORLD_REWRITE,
    // ---- 22. Admin ----
    JUDGEMENT_STRIKE,

    // ---- 23. Phantom ----
    NIGHTMARE_GLIDE,
    // ---- 24. Shulker ----
    LEVITATION_MATRIX,
    // ---- 25. Slime ----
    BOUNCE_OVERLORD,
    // ---- 26. Drowned ----
    TRIDENT_TEMPEST,
    // ---- 27. Magma Cube ----
    VOLCANIC_STOMP,
    // ---- 28. Iron Golem ----
    FORTRESS_VANGUARD,
    // ---- 29. Ghast ----
    HELLFIRE_ARTILLERY,
    // ---- 30. Piglin Brute ----
    GOLDEN_RETRIBUTION,
    // ---- 31. Evoker ----
    TOTEM_SUMMONER,
    // ---- 32. Vex ----
    WRATHFUL_SPIRIT
}
