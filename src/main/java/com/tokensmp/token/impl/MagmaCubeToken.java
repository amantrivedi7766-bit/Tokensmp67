package com.tokensmp.token.impl;

import com.tokensmp.core.ConfigManager;
import com.tokensmp.token.AbilityTrigger;
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
 * Magma Cube Token (Rare).
 * Passive: Fire/Lava speed, complete Fire Immunity, melee ignites.
 * Ability: Volcanic Stomp - Ground smash that turns a 3x3 area to burning magma.
 */
public final class MagmaCubeToken extends AbstractToken {

    public MagmaCubeToken(ConfigManager config) {
        super("magma_cube", "Magma Cube", TokenRarity.RARE, Material.MAGMA_CREAM, false, config);

        addTier(TokenTier.of(1)
                .potion(speed(), 0)
                .fireImmunity(true)
                .burningAura(true)
                .passiveDescription("Fire/Lava speed, complete Fire Immunity, melee ignites")
                .ability(new TokenTier.AbilitySpec(TokenAbility.VOLCANIC_STOMP, "Volcanic Stomp",
                        anum(1, "cooldown", 30))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 22))
                        .range(adnum(1, "range", 8.0))
                        .radius(adnum(1, "radius", 4.0))
                        .knockback(adnum(1, "knockback", 1.2))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 4.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 50))
                        .particles(aparticles(1, Particle.LAVA, Particle.FLAME, Particle.SMOKE))
                        .sounds(asounds(1, Sound.ENTITY_MAGMA_CUBE_JUMP, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Ground smash that turns a 3x3 area to burning magma"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.MAGMA_CUBE),
                        num("tier1.task-count", 350), "Kill 350 Magma Cubes")
                .cost(Material.MAGMA_CREAM, num("tier1.cost-magma-cream", 32))
                .build());

        addTier(TokenTier.of(2)
                .potion(speed(), 0)
                .fireImmunity(true)
                .burningAura(true)
                .passiveDescription("Fire/Lava speed, complete Fire Immunity, melee ignites")
                .ability(new TokenTier.AbilitySpec(TokenAbility.VOLCANIC_STOMP, "Volcanic Stomp",
                        anum(2, "cooldown", 26))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 36))
                        .range(adnum(2, "range", 8.0))
                        .radius(adnum(2, "radius", 5.0))
                        .knockback(adnum(2, "knockback", 1.5))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 5.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 50))
                        .particles(aparticles(2, Particle.LAVA, Particle.FLAME, Particle.SMOKE))
                        .sounds(asounds(2, Sound.ENTITY_MAGMA_CUBE_JUMP, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Ground smash that turns a 3x3 area to burning magma"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.MAGMA_CUBE),
                        num("tier2.task-count", 900), "Kill 900 Magma Cubes")
                .cost(Material.MAGMA_BLOCK, num("tier2.cost-magma-block", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(speed(), 0)
                .fireImmunity(true)
                .burningAura(true)
                .passiveDescription("Fire/Lava speed, complete Fire Immunity, melee ignites")
                .ability(new TokenTier.AbilitySpec(TokenAbility.VOLCANIC_STOMP, "Volcanic Stomp",
                        anum(3, "cooldown", 22))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 52))
                        .range(adnum(3, "range", 8.0))
                        .radius(adnum(3, "radius", 6.0))
                        .knockback(adnum(3, "knockback", 1.8))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 6.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, Particle.LAVA, Particle.FLAME, Particle.SMOKE))
                        .sounds(asounds(3, Sound.ENTITY_MAGMA_CUBE_JUMP, Sound.ENTITY_GENERIC_EXPLODE))
                        .description("Ground smash that turns a 3x3 area to burning magma"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.MAGMA_CUBE),
                        num("tier3.task-count", 2200), "Kill 2200 Magma Cubes")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
