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
 * Bat Token (Common).
 * Passive: Permanent Night Vision, Slow Falling, Speed II.
 * Ability: Echolocation - 40-block sonar pulse that outlines every nearby entity for 10s.
 */
public final class BatToken extends AbstractToken {

    public BatToken(ConfigManager config) {
        super("bat", "Bat", TokenRarity.COMMON, Material.BAT_SPAWN_EGG, false, config);

        addTier(TokenTier.of(1)
                .potion(nightVision(), 0)
                .potion(slowFalling(), 0)
                .potion(speed(), 1)
                .passiveDescription("Permanent Night Vision, Slow Falling, Speed II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.ECHOLOCATION, "Echolocation",
                        anum(1, "cooldown", 30))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 4))
                        .range(adnum(1, "range", 40.0))
                        .radius(adnum(1, "radius", 40.0))
                        .knockback(adnum(1, "knockback", 0.0))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 10.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 50))
                        .particles(aparticles(1, Particle.SCULK_SOUL, Particle.SQUID_INK))
                        .sounds(asounds(1, Sound.ENTITY_BAT_AMBIENT))
                        .description("40-block sonar pulse that outlines every nearby entity for 10s"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BAT),
                        num("tier1.task-count", 400), "Kill 400 Bats")
                .cost(Material.PHANTOM_MEMBRANE, num("tier1.cost-phantom-membrane", 16))
                .build());

        addTier(TokenTier.of(2)
                .potion(nightVision(), 0)
                .potion(slowFalling(), 0)
                .potion(speed(), 1)
                .passiveDescription("Permanent Night Vision, Slow Falling, Speed II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.ECHOLOCATION, "Echolocation",
                        anum(2, "cooldown", 26))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 8))
                        .range(adnum(2, "range", 40.0))
                        .radius(adnum(2, "radius", 40.0))
                        .knockback(adnum(2, "knockback", 0.0))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 10.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 50))
                        .particles(aparticles(2, Particle.SCULK_SOUL, Particle.SQUID_INK))
                        .sounds(asounds(2, Sound.ENTITY_BAT_AMBIENT))
                        .description("40-block sonar pulse that outlines every nearby entity for 10s"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BAT),
                        num("tier2.task-count", 1000), "Kill 1000 Bats")
                .cost(Material.COAL, num("tier2.cost-coal", 64))
                .build());

        addTier(TokenTier.of(3)
                .potion(nightVision(), 0)
                .potion(slowFalling(), 0)
                .potion(speed(), 1)
                .passiveDescription("Permanent Night Vision, Slow Falling, Speed II")
                .ability(new TokenTier.AbilitySpec(TokenAbility.ECHOLOCATION, "Echolocation",
                        anum(3, "cooldown", 22))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 12))
                        .range(adnum(3, "range", 40.0))
                        .radius(adnum(3, "radius", 40.0))
                        .knockback(adnum(3, "knockback", 0.0))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 10.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, Particle.SCULK_SOUL, Particle.SQUID_INK))
                        .sounds(asounds(3, Sound.ENTITY_BAT_AMBIENT))
                        .description("40-block sonar pulse that outlines every nearby entity for 10s"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.BAT),
                        num("tier3.task-count", 2500), "Kill 2500 Bats")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
