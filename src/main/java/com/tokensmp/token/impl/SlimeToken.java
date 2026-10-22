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
 * Slime Token (Common).
 * Passive: Infinite Jump Boost V, No Fall Damage, projectile resistance.
 * Ability: Bounce Overlord - Ground jump shockwave that throws everything 20 blocks away.
 */
public final class SlimeToken extends AbstractToken {

    public SlimeToken(ConfigManager config) {
        super("slime", "Slime", TokenRarity.COMMON, Material.SLIME_BALL, false, config);

        addTier(TokenTier.of(1)
                .potion(jumpBoost(), 4)
                .fallImmunity(true)
                .potion(resistance(), 0)
                .passiveDescription("Infinite Jump Boost V, No Fall Damage, projectile resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BOUNCE_OVERLORD, "Bounce Overlord",
                        anum(1, "cooldown", 30))
                        .trigger(atrigger(1, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(1, "damage", 20))
                        .range(adnum(1, "range", 12.0))
                        .radius(adnum(1, "radius", 8.0))
                        .knockback(adnum(1, "knockback", 2.5))
                        .count(anum(1, "count", 1))
                        .speed(adnum(1, "speed", 1.0))
                        .duration(adnum(1, "duration", 1.0))
                        .trueDamage(abool(1, "true-damage", false))
                        .particleCount(anum(1, "particle-count", 50))
                        .particles(aparticles(1, Particle.ITEM_SLIME, Particle.SPLASH, Particle.CLOUD))
                        .sounds(asounds(1, Sound.ENTITY_SLIME_JUMP, Sound.ENTITY_SLIME_ATTACK))
                        .description("Ground jump shockwave that throws everything 20 blocks away"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SLIME),
                        num("tier1.task-count", 400), "Kill 400 Slimes")
                .cost(Material.SLIME_BALL, num("tier1.cost-slime-ball", 64))
                .build());

        addTier(TokenTier.of(2)
                .potion(jumpBoost(), 4)
                .fallImmunity(true)
                .potion(resistance(), 0)
                .passiveDescription("Infinite Jump Boost V, No Fall Damage, projectile resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BOUNCE_OVERLORD, "Bounce Overlord",
                        anum(2, "cooldown", 26))
                        .trigger(atrigger(2, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(2, "damage", 32))
                        .range(adnum(2, "range", 12.0))
                        .radius(adnum(2, "radius", 12.0))
                        .knockback(adnum(2, "knockback", 3.0))
                        .count(anum(2, "count", 1))
                        .speed(adnum(2, "speed", 1.0))
                        .duration(adnum(2, "duration", 1.0))
                        .trueDamage(abool(2, "true-damage", false))
                        .particleCount(anum(2, "particle-count", 50))
                        .particles(aparticles(2, Particle.ITEM_SLIME, Particle.SPLASH, Particle.CLOUD))
                        .sounds(asounds(2, Sound.ENTITY_SLIME_JUMP, Sound.ENTITY_SLIME_ATTACK))
                        .description("Ground jump shockwave that throws everything 20 blocks away"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SLIME),
                        num("tier2.task-count", 1000), "Kill 1000 Slimes")
                .cost(Material.SLIME_BLOCK, num("tier2.cost-slime-block", 16))
                .build());

        addTier(TokenTier.of(3)
                .potion(jumpBoost(), 4)
                .fallImmunity(true)
                .potion(resistance(), 0)
                .passiveDescription("Infinite Jump Boost V, No Fall Damage, projectile resistance")
                .ability(new TokenTier.AbilitySpec(TokenAbility.BOUNCE_OVERLORD, "Bounce Overlord",
                        anum(3, "cooldown", 22))
                        .trigger(atrigger(3, AbilityTrigger.SHIFT_RIGHT_CLICK))
                        .damage(anum(3, "damage", 48))
                        .range(adnum(3, "range", 12.0))
                        .radius(adnum(3, "radius", 16.0))
                        .knockback(adnum(3, "knockback", 3.5))
                        .count(anum(3, "count", 1))
                        .speed(adnum(3, "speed", 1.0))
                        .duration(adnum(3, "duration", 1.0))
                        .trueDamage(abool(3, "true-damage", false))
                        .particleCount(anum(3, "particle-count", 50))
                        .particles(aparticles(3, Particle.ITEM_SLIME, Particle.SPLASH, Particle.CLOUD))
                        .sounds(asounds(3, Sound.ENTITY_SLIME_JUMP, Sound.ENTITY_SLIME_ATTACK))
                        .description("Ground jump shockwave that throws everything 20 blocks away"))
                .task(TokenTier.TaskType.KILLS, List.of(EntityType.SLIME),
                        num("tier3.task-count", 2500), "Kill 2500 Slimes")
                .cost(Material.NETHERITE_INGOT, num("tier3.cost-netherite-ingot", 4))
                .build());

    }
}
