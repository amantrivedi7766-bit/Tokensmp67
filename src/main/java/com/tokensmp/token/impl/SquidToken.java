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
 * Squid Token (Common).
 * Passive: Dolphin's Grace II water mobility.
 * Ability: Ink Cloud - Blinding ink blast - Blindness + Slowness in 10 blocks.
 */
public final class SquidToken extends AbstractToken {

    public SquidToken(ConfigManager config) {
        super("squid", "Squid", TokenRarity.COMMON, Material.INK_SAC, false, config);

        addTier(TokenTier.of(1)
                .potion(dolphinsGrace(), 1)
                .passiveDescription("Dolphin's Grace II water mobility")
                .ability(new TokenTier.AbilitySpec(TokenAbility.INK_CLOUD, "Ink Cloud",
                        anum(1, "cooldown", 30))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 10))
                        .range(adnum(1, "range", 10.0))
                        .radius(adnum(1, "radius", 10.0))
                        .knockback(adnum(1, "knockback", 0.6))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 4.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 60))
                        .particles(aparticles(1, Particle.SQUID_INK, Particle.CLOUD, Particle.BUBBLE))
                        .sounds(asounds(1, Sound.ENTITY_SQUID_SQUIRT))
                        .description("Blinding ink blast - Blindness + Slowness in 10 blocks"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SQUID),
                        num("tier1.task-count", 400), "Kill 400 Squids")
                .cost(Material.INK_SAC, num("tier1.cost-ink-sac", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(dolphinsGrace(), 1)
                .passiveDescription("Dolphin's Grace II water mobility")
                .ability(new TokenTier.AbilitySpec(TokenAbility.INK_CLOUD, "Ink Cloud",
                        anum(2, "cooldown", 26))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 18))
                        .range(adnum(2, "range", 10.0))
                        .radius(adnum(2, "radius", 10.0))
                        .knockback(adnum(2, "knockback", 0.8))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 6.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 60))
                        .particles(aparticles(2, Particle.SQUID_INK, Particle.CLOUD, Particle.BUBBLE))
                        .sounds(asounds(2, Sound.ENTITY_SQUID_SQUIRT))
                        .description("Blinding ink blast - Blindness + Slowness in 10 blocks"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SQUID),
                        num("tier2.task-count", 1000), "Kill 1000 Squids")
                .cost(Material.PRISMARINE_CRYSTALS, num("tier2.cost-prismarine-crystals", 32))
                .build());

        addTier(TokenTier.of(3)
                .potion(dolphinsGrace(), 1)
                .passiveDescription("Dolphin's Grace II water mobility")
                .ability(new TokenTier.AbilitySpec(TokenAbility.INK_CLOUD, "Ink Cloud",
                        anum(3, "cooldown", 22))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 26))
                        .range(adnum(3, "range", 10.0))
                        .radius(adnum(3, "radius", 10.0))
                        .knockback(adnum(3, "knockback", 1.0))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 8.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 60))
                        .particles(aparticles(3, Particle.SQUID_INK, Particle.CLOUD, Particle.BUBBLE))
                        .sounds(asounds(3, Sound.ENTITY_SQUID_SQUIRT))
                        .description("Blinding ink blast - Blindness + Slowness in 10 blocks"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SQUID),
                        num("tier3.task-count", 2500), "Kill 2500 Squids")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
