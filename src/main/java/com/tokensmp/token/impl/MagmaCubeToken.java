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
 * Magma Cube Token (Rare): molten identity. T1 Magma Slam, T2 Magma Wave, T3 Magma Core Eruption.
 */
public final class MagmaCubeToken extends AbstractToken {

    public MagmaCubeToken(ConfigManager config) {
        super("magma_cube", "Magma Cube", TokenRarity.RARE, Material.MAGMA_CREAM, false, config);

        addTier(TokenTier.of(1)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.MAGMA_SLAM, "Magma Slam",
                        anum(1, "cooldown", 25))
                        .damage(anum(1, "damage", 16))
                        .range(adnum(1, "range", 6.0))
                        .radius(adnum(1, "radius", 4.0))
                        .knockback(adnum(1, "knockback", 1.4))
                        .count(anum(1, "projectiles", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 30))
                        .particles(aparticles(1, Particle.FLAME, Particle.LAVA))
                        .sounds(asounds(1, Sound.ENTITY_MAGMA_CUBE_JUMP, Sound.ENTITY_MAGMA_CUBE_SQUISH))
                        .description("Molten ground strike: 16 damage in a small molten burst"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.MAGMA_CUBE),
                        num("tier1.task-count", 350), "Kill 350 Magma Cubes")
                .cost(Material.MAGMA_CREAM, num("tier1.cost-magma-cream", 32))
                .build());

        addTier(TokenTier.of(2)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.MAGMA_WAVE, "Magma Wave",
                        anum(2, "cooldown", 43))
                        .damage(anum(2, "damage", 28))
                        .range(adnum(2, "range", 16.0))
                        .radius(adnum(2, "radius", 3.0))
                        .knockback(adnum(2, "knockback", 1.6))
                        .count(anum(2, "projectiles", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 36))
                        .particles(aparticles(2, Particle.LAVA, Particle.FLAME, Particle.SMOKE))
                        .sounds(asounds(2, Sound.ENTITY_MAGMA_CUBE_SQUISH, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("A low molten wave travels forward: 28 damage along a wide ground lane"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.MAGMA_CUBE),
                        num("tier2.task-count", 900), "Kill 900 Magma Cubes")
                .cost(Material.MAGMA_CREAM, num("tier2.cost-magma-cream", 64))
                .build());

        addTier(TokenTier.of(3)
                .potion(fireResistance(), 0)
                .passiveDescription("Fire Resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.MAGMA_CORE_ERUPTION, "Magma Core Eruption",
                        anum(3, "cooldown", 65))
                        .damage(anum(3, "damage", 50))
                        .range(adnum(3, "range", 22.0))
                        .radius(adnum(3, "radius", 6.0))
                        .knockback(adnum(3, "knockback", 2.2))
                        .count(anum(3, "projectiles", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 48))
                        .particles(aparticles(3, Particle.LAVA, Particle.FLAME, Particle.SOUL_FIRE_FLAME))
                        .sounds(asounds(3, Sound.ENTITY_MAGMA_CUBE_JUMP, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("A molten core erupts beneath the target: 50 damage + molten shockwave"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.MAGMA_CUBE),
                        num("tier3.task-count", 2200), "Kill 2,200 Magma Cubes")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());
    }
}
