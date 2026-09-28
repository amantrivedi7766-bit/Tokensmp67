package com.tokensmp.token;

/**
 * The distinct ability behaviors implemented by the ability package. Every
 * token's Tier 3 ability maps to exactly one of these - implementations are
 * mechanically distinct (shockwave, barrage, harpoon, nova, teleport strike,
 * lance, sonic cone, skull barrage, slam, dive, beam, fangs, boomerang,
 * fireball, meteor rain, potions, chain dash, charge, wind burst, curse
 * beam) and every one causes real physical server-side damage.
 */
public enum TokenAbility {

    /** No active ability (pure passive tier). */
    NONE,

    // ------------------------------------------------------------------
    // 20 player token abilities - all physical, all unique mechanics
    // ------------------------------------------------------------------

    /** Zombie T3: undead shockwave centered on the player (AoE damage + KB). */
    UNDEAD_SHOCKWAVE,

    /** Skeleton T3: multiple real arrow projectiles with different trajectories. */
    ARROW_BARRAGE,

    /** Spider T3: web projectile that pulls the victim toward the impact point. */
    WEB_HARPOON,

    /** Creeper T3: charge-up, warning ring, expanding detonation nova. */
    VOLATILE_SURGE,

    /** Enderman T3: teleport behind the target, dimensional slash, void pulse. */
    VOID_RIFT,

    /** Blaze T3: piercing charged fire projectile along a line. */
    INFERNO_LANCE,

    /** Warden T3: sonic cone blast, true damage + massive knockback. */
    SONIC_OBLITERATION,

    /** Wither T3: multiple wither skull projectiles on different trajectories. */
    WITHER_BARRAGE,

    /** Slime T3: leap up and slam down, AoE damage + launch victims. */
    BOUNCE_SLAM,

    /** Phantom T3: forward swooping dash damaging everything on the path. */
    PHANTOM_DIVE,

    /** Guardian T3: target lock, charging beam, instant damage on the locked target. */
    PRISMATIC_BEAM,

    /** Evoker T3: a line of real evoker fangs advancing toward the target. */
    FANG_VOLLEY,

    /** Piglin T3: thrown returning axe projectile (boomerang). */
    GILDED_AXE,

    /** Ghast T3: large explosive fireball projectile (no terrain damage). */
    FIREBALL_LAUNCH,

    /** Magma Cube T3: multiple small magma meteors raining on an area. */
    METEOR_SPLIT,

    /** Witch T3: real splash potion volley (poison / slowness / harming). */
    HEX_BREW,

    /** Vindicator T3: chain dash strikes between nearby enemies. */
    AXE_RAMPAGE,

    /** Ravager T3: directional charge, collision damage + ground shockwave. */
    RAVAGER_CHARGE,

    /** Breeze T3: cyclone burst launching enemies into the air. */
    CYCLONE_BURST,

    /** Elder Guardian T3: sustained curse beam, damage over time + fatigue. */
    CURSE_BEAM,

    // ------------------------------------------------------------------
    // Admin token abilities
    // ------------------------------------------------------------------

    /** Admin T1: nether shockwave around the user (Nether Overlord). */
    NETHER_SHOCKWAVE,

    /** Admin T2: freezes every nearby player in time (Ruler of the Realm). */
    CHRONO_FREEZE,

    /** Admin T3: god-tier forward judgment ray (Server Judgment). */
    SERVER_JUDGMENT
}
