package com.tokensmp.token.impl;

import com.tokensmp.core.ConfigManager;
import com.tokensmp.token.AbstractToken;
import com.tokensmp.token.TokenAbility;
import com.tokensmp.token.TokenRarity;
import com.tokensmp.token.TokenTier;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;

import java.util.List;

/**
 * Fish Token (Common): aquatic identity. T1 Aqua Bullet, T2 Tidal Ram, T3 Leviathan Crash.
 */
public final class FishToken extends AbstractToken {

    public FishToken(ConfigManager config) {
        super("fish", "Fish", TokenRarity.COMMON, Material.TROPICAL_FISH, false, config);

        addTier(TokenTier.of(1)
                .potion(waterBreathing(), 0)
                .passiveDescription("Water Breathing")
                .ability(new TokenTier.AbilitySpec(TokenAbility.AQUA_BULLET, "Aqua Bullet",
                        anum(1, "cooldown", 20))
                        .damage(anum(1, "damage", 10))
                        .range(adnum(1, "range", 18.0))
                        .radius(adnum(1, "radius", 2.0))
                        .knockback(adnum(1, "knockback", 0.5))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.5))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 26))
                        .particles(aparticles(1, Particle.SPLASH, Particle.BUBBLE))
                        .sounds(asounds(1, Sound.ENTITY_PLAYER_SPLASH, Sound.ENTITY_GENERIC_SPLASH))
                        .description("Fire a compressed water projectile: 10 damage, circular splash on impact"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.COD, EntityType.SALMON, EntityType.TROPICAL_FISH, EntityType.PUFFERFISH),
                        num("tier1.task-count", 300), "Kill 300 Fish")
                .cost(Material.COD, num("tier1.cost-cod", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(waterBreathing(), 0)
                .passiveDescription("Water Breathing")
                .ability(new TokenTier.AbilitySpec(TokenAbility.TIDAL_RAM, "Tidal Ram",
                        anum(2, "cooldown", 35))
                        .damage(anum(2, "damage", 21))
                        .range(adnum(2, "range", 16.0))
                        .radius(adnum(2, "radius", 2.5))
                        .knockback(adnum(2, "knockback", 1.2))
                        .count(anum(2, "projectiles", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 34))
                        .particles(aparticles(2, Particle.SPLASH, Particle.BUBBLE))
                        .sounds(asounds(2, Sound.ENTITY_PLAYER_SPLASH, Sound.ENTITY_GENERIC_SPLASH))
                        .description("A moving water wave sweeps the ground: 21 damage to everything it passes"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.COD, EntityType.SALMON, EntityType.TROPICAL_FISH, EntityType.PUFFERFISH),
                        num("tier2.task-count", 800), "Kill 800 Fish")
                .cost(Material.PRISMARINE_SHARD, num("tier2.cost-prismarine-shard", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(waterBreathing(), 0)
                .passiveDescription("Water Breathing")
                .ability(new TokenTier.AbilitySpec(TokenAbility.LEVIATHAN_CRASH, "Leviathan Crash",
                        anum(3, "cooldown", 55))
                        .damage(anum(3, "damage", 36))
                        .range(adnum(3, "range", 22.0))
                        .radius(adnum(3, "radius", 6.0))
                        .knockback(adnum(3, "knockback", 2.0))
                        .count(anum(3, "projectiles", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 44))
                        .particles(aparticles(3, Particle.SPLASH, Particle.BUBBLE, Particle.CLOUD))
                        .sounds(asounds(3, Sound.ENTITY_GENERIC_SPLASH, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("A water column forms above the target then crashes: 36 damage in a large radius"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.COD, EntityType.SALMON, EntityType.TROPICAL_FISH, EntityType.PUFFERFISH),
                        num("tier3.task-count", 2000), "Kill 2,000 Fish")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());
    }
}
