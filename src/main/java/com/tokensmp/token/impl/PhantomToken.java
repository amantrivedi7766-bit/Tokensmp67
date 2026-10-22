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
 * Phantom Token (Epic).
 * Passive: Infinite Slow Falling, slow flight, Night Vision III.
 * Ability: Nightmare Glide - Phase through blocks for 7s, then swoop-strike the target.
 */
public final class PhantomToken extends AbstractToken {

    public PhantomToken(ConfigManager config) {
        super("phantom", "Phantom", TokenRarity.EPIC, Material.PHANTOM_MEMBRANE, false, config);

        addTier(TokenTier.of(1)
                .potion(slowFalling(), 0)
                .flight(true)
                .potion(nightVision(), 2)
                .passiveDescription("Infinite Slow Falling, slow flight, Night Vision III")
                .ability(new TokenTier.AbilitySpec(TokenAbility.NIGHTMARE_GLIDE, "Nightmare Glide",
                        anum(1, "cooldown", 40))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 25))
                        .range(adnum(1, "range", 10.0))
                        .radius(adnum(1, "radius", 4.0))
                        .knockback(adnum(1, "knockback", 1.2))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 7.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 40))
                        .particles(aparticles(1, Particle.CLOUD, Particle.SQUID_INK, Particle.END_ROD))
                        .sounds(asounds(1, Sound.ENTITY_PHANTOM_BITE, Sound.ENTITY_PHANTOM_FLAP))
                        .description("Phase through blocks for 7s, then swoop-strike the target"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.PHANTOM),
                        num("tier1.task-count", 100), "Kill 100 Phantoms")
                .cost(Material.PHANTOM_MEMBRANE, num("tier1.cost-phantom-membrane", 16))
                .build());

        addTier(TokenTier.of(2)
                .potion(slowFalling(), 0)
                .flight(true)
                .potion(nightVision(), 2)
                .passiveDescription("Infinite Slow Falling, slow flight, Night Vision III")
                .ability(new TokenTier.AbilitySpec(TokenAbility.NIGHTMARE_GLIDE, "Nightmare Glide",
                        anum(2, "cooldown", 34))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 40))
                        .range(adnum(2, "range", 10.0))
                        .radius(adnum(2, "radius", 5.0))
                        .knockback(adnum(2, "knockback", 1.5))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 7.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 40))
                        .particles(aparticles(2, Particle.CLOUD, Particle.SQUID_INK, Particle.END_ROD))
                        .sounds(asounds(2, Sound.ENTITY_PHANTOM_BITE, Sound.ENTITY_PHANTOM_FLAP))
                        .description("Phase through blocks for 7s, then swoop-strike the target"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.PHANTOM),
                        num("tier2.task-count", 300), "Kill 300 Phantoms")
                .cost(Material.PHANTOM_MEMBRANE, num("tier2.cost-phantom-membrane", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(slowFalling(), 0)
                .flight(true)
                .potion(nightVision(), 2)
                .passiveDescription("Infinite Slow Falling, slow flight, Night Vision III")
                .ability(new TokenTier.AbilitySpec(TokenAbility.NIGHTMARE_GLIDE, "Nightmare Glide",
                        anum(3, "cooldown", 28))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 60))
                        .range(adnum(3, "range", 10.0))
                        .radius(adnum(3, "radius", 6.0))
                        .knockback(adnum(3, "knockback", 1.8))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 7.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 40))
                        .particles(aparticles(3, Particle.CLOUD, Particle.SQUID_INK, Particle.END_ROD))
                        .sounds(asounds(3, Sound.ENTITY_PHANTOM_BITE, Sound.ENTITY_PHANTOM_FLAP))
                        .description("Phase through blocks for 7s, then swoop-strike the target"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.PHANTOM),
                        num("tier3.task-count", 800), "Kill 800 Phantoms")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
